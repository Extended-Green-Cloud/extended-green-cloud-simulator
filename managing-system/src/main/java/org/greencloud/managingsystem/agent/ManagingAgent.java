package org.greencloud.managingsystem.agent;

import static java.lang.Double.parseDouble;
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

import org.greencloud.commons.args.scenario.ScenarioStructureArgs;

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

	@Override
	protected void initializeAgent(final Object[] args) {
		disabledByDefaultActions = new ArrayList<>();

		if (args.length >= 4) {
			try {
				this.systemQualityThreshold = parseDouble(args[0].toString());
				this.greenCloudStructure = (ScenarioStructureArgs) args[1];
				this.greenCloudController = (ContainerController) args[2];
				parseAdditionalParameters(args);
			} catch (NumberFormatException e) {
				logger.info("Incorrect argument: please check arguments in the documentation");
				doDelete();
			}
			getContentManager().registerLanguage(new SLCodec());
			getContentManager().registerOntology(MobilityOntology.getInstance());
		} else {
			logger.info("Incorrect arguments: some parameters for Managing Agent are missing");
			doDelete();
		}

		this.monitoringService = new MonitoringService(this);
		this.analyzerService = new AnalyzerService(this);
		this.plannerService = new PlannerService(this);
		this.executorService = new ExecutorService(this);
		this.mobilityService = new MobilityService(this);
	}

	@Override
	protected void validateAgentArguments() {
		if (systemQualityThreshold <= 0 || systemQualityThreshold > 1) {
			logger.info("Incorrect argument: System quality must be from a range (0,1]");
			doDelete();
		}
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return List.of(
				new ReadAdaptationGoals(),
				new DisableAdaptationActions(this, disabledByDefaultActions)
		);
	}

	@SuppressWarnings({ "unchecked", "static" })
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
}
