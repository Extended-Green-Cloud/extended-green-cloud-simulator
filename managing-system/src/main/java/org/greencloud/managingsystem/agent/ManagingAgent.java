package org.greencloud.managingsystem.agent;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.greencloud.managingsystem.service.planner.domain.AdaptationPlanVariables.POWER_SHORTAGE_THRESHOLD;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.greencloud.managingsystem.agent.behaviour.knowledge.DisableAdaptationActions;
import org.greencloud.managingsystem.agent.behaviour.knowledge.ReadAdaptationGoals;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.behaviours.ReceiveGUIController;
import com.greencloud.commons.scenario.ScenarioStructureArgs;

import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;

/**
 * Agent representing the Managing Agent that is responsible for the system's adaptation
 */
public class ManagingAgent extends AbstractManagingAgent {

	private static final Logger logger = LoggerFactory.getLogger(ManagingAgent.class);
	private List<String> disabledByDefaultActions;

	/**
	 * Method initializes the agents and start the behaviour which upon connecting with the agent node, reads
	 * the adaptation goals from the database
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		disabledByDefaultActions = new ArrayList<>();
		initializeAgent(getArguments());
		addBehaviour(new ReceiveGUIController(this, behavioursRunAtStart()));
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(MobilityOntology.getInstance());
		containersLocations = findContainersLocations();
	}

	/**
	 * Method logs the information to the console.
	 */
	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		getGuiController().removeAgentNodeFromGraph(getAgentNode());
		super.takeDown();
	}

	private void initializeAgent(final Object[] args) {
		if (Objects.nonNull(args) && args.length >= 3) {
			try {
				final double systemQuality = Double.parseDouble(args[0].toString());

				if (systemQuality <= 0 || systemQuality > 1) {
					logger.info("Incorrect argument: System quality must be from a range (0,1]");
					doDelete();
				}
				this.systemQualityThreshold = systemQuality;
				this.greenCloudStructure = (ScenarioStructureArgs) args[1];
				this.greenCloudController = (ContainerController) args[2];

				if (args.length > 3) {
					// in separate if as more params will be added
					if (Objects.nonNull(args[3])) {
						POWER_SHORTAGE_THRESHOLD = Integer.parseInt(String.valueOf(args[3]));
					}

					if (Objects.nonNull(args[4])) {
						disabledByDefaultActions = (ArrayList<String>) args[4];
					}
				}

			} catch (NumberFormatException e) {
				logger.info("Incorrect argument: please check arguments in the documentation");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for green source agent are missing - "
					+ "check the parameters in the documentation");
			doDelete();
		}
	}

	private List<Behaviour> behavioursRunAtStart() {
		return List.of(
				new ReadAdaptationGoals(),
				new DisableAdaptationActions(this, disabledByDefaultActions)
		);
	}

	private List<Location> findContainersLocations() {
		prepareAndSendPlatformLocationsQuery();
		Result result = receiveLocationsResponseFromAms();
		List<Location> list = getLocationsFromQueryResult(result);
		logger.info("Found {} containers: {}.", list.size(),
				list.stream().map(Location::getName).collect(joining(", ")));
		return list;
	}

	private void prepareAndSendPlatformLocationsQuery() {
		Action queryLocations = new Action(getAMS(), new QueryPlatformLocationsAction());
		ACLMessage locationsRequest = new ACLMessage(ACLMessage.REQUEST);
		locationsRequest.setLanguage(new SLCodec().getName());
		locationsRequest.setOntology(MobilityOntology.getInstance().getName());
		try {
			getContentManager().fillContent(locationsRequest, queryLocations);
			locationsRequest.addReceiver(queryLocations.getActor());
			send(locationsRequest);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Nullable
	private Result receiveLocationsResponseFromAms() {
		ACLMessage response = blockingReceive(and(MatchSender(getAMS()), MatchPerformative(INFORM)));
		ContentElement contentElement = null;
		try {
			contentElement = getContentManager().extractContent(response);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return (Result) contentElement;
	}

	private List<Location> getLocationsFromQueryResult(@Nullable Result result) {
		if (isNull(result)) {
			return emptyList();
		}
		List<Location> locations = new ArrayList<>();
		jade.util.leap.Iterator it = result.getItems().iterator();
		while (it.hasNext()) {
			Location loc = (Location) it.next();
			locations.add(loc);
		}
		return locations;
	}
}
