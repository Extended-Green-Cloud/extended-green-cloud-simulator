package org.greencloud.managingsystem.agent;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;
import static org.greencloud.managingsystem.service.planner.plans.domain.AdaptationPlanVariables.POWER_SHORTAGE_THRESHOLD;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.greencloud.managingsystem.agent.behaviour.knowledge.DisableAdaptationActions;
import org.greencloud.managingsystem.agent.behaviour.knowledge.ReadAdaptationGoals;
import org.greencloud.managingsystem.service.analyzer.AnalyzerService;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.greencloud.managingsystem.service.mobility.MobilityService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.PlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.behaviours.ReceiveGUIController;
import com.greencloud.commons.scenario.ScenarioStructureArgs;

import jade.content.lang.sl.SLCodec;
import jade.core.behaviours.Behaviour;
import jade.domain.mobility.MobilityOntology;
import jade.wrapper.ContainerController;

/**
 * Agent representing the Managing Agent that is responsible for the system's adaptation
 */
public class ManagingAgent extends AbstractManagingAgent {

	private static final Logger logger = LoggerFactory.getLogger(ManagingAgent.class);
	private List<String> disabledByDefaultActions;

	/**
	 * Method initializes the agent and starts the behaviour which upon connecting with the agent node, reads
	 * the adaptation goals from the database
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		initializeAgent(getArguments());
		addBehaviour(new ReceiveGUIController(this, behavioursRunAtStart()));
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(MobilityOntology.getInstance());
	}

	private void initializeAgent(final Object[] args) {
		disabledByDefaultActions = new ArrayList<>();

		if (Objects.nonNull(args) && args.length >= 3) {
			try {
				final double systemQuality = Double.parseDouble(args[0].toString());
				validateSystemQualityThreshold(systemQuality);
				this.systemQualityThreshold = systemQuality;
				this.greenCloudStructure = (ScenarioStructureArgs) args[1];
				this.greenCloudController = (ContainerController) args[2];
				parseAdditionalParameters(args);

			} catch (NumberFormatException e) {
				logger.info("Incorrect argument: please check arguments in the documentation");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for green source agent are missing - "
						+ "check the parameters in the documentation");
			doDelete();
		}

		this.monitoringService = new MonitoringService(this);
		this.analyzerService = new AnalyzerService(this);
		this.plannerService = new PlannerService(this);
		this.executorService = new ExecutorService(this);
		this.mobilityService = new MobilityService(this);
	}

	private void validateSystemQualityThreshold(final double systemQuality) {
		if (systemQuality <= 0 || systemQuality > 1) {
			logger.info("Incorrect argument: System quality must be from a range (0,1]");
			doDelete();
		}
	}

	@SuppressWarnings({"unchecked", "static"})
	private void parseAdditionalParameters(final Object[] args) {
		if (args.length > 3) {
			if (Objects.nonNull(args[3])) {
				POWER_SHORTAGE_THRESHOLD = Integer.parseInt(String.valueOf(args[3]));
			}

			if (Objects.nonNull(args[4])) {
				disabledByDefaultActions = (ArrayList<String>) args[4];
			}
		}
	}


	private List<Behaviour> behavioursRunAtStart() {
		return List.of(
				new ReadAdaptationGoals(),
				new DisableAdaptationActions(this, disabledByDefaultActions)
		);
	}
}
