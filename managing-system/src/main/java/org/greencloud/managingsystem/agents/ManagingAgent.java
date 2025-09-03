package org.greencloud.managingsystem.agents;

import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.util.List;

import org.apache.commons.lang3.DoubleRange;
import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.managing.agent.ManagingAgentProps;
import org.greencloud.commons.args.agent.managing.factory.ManagingAgentArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.behaviours.knowledge.DisableAdaptationActions;
import org.greencloud.managingsystem.behaviours.knowledge.ReadAdaptationGoals;
import org.greencloud.managingsystem.service.analyzer.AnalyzerService;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.greencloud.managingsystem.service.mobility.MobilityService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.PlannerService;

import com.greencloud.connector.factory.EGCSControllerFactory;

import jade.content.lang.sl.SLCodec;
import jade.core.behaviours.Behaviour;
import jade.domain.mobility.MobilityOntology;

/**
 * Agent representing the Managing Agent that is responsible for the system's adaptation.
 */
public class ManagingAgent extends EGCSAgent<ManagingAgentNode, ManagingAgentProps, ManagingAgentArgs> {

	private static final LogBuilder log = constructLogBuilder(ManagingAgent.class);

	private MonitoringService monitoringService;
	private AnalyzerService analyzerService;
	private PlannerService plannerService;
	private ExecutorService executorService;
	private MobilityService mobilityService;

	public ManagingAgent() {
		super();
	}

	@Override
	protected void initializeAgent(final Object[] args) {
		try {
			final ManagingAgentArgs managingAgentArgs = (ManagingAgentArgs) args[0];
			final ScenarioStructureArgs greenCloudStructure = (ScenarioStructureArgs) args[1];
			final EGCSControllerFactory controllerFactory = (EGCSControllerFactory) args[2];

			getContentManager().registerLanguage(new SLCodec());
			getContentManager().registerOntology(MobilityOntology.getInstance());

			this.properties = new ManagingAgentProps(getName(), managingAgentArgs, greenCloudStructure);
			this.monitoringService = new MonitoringService(this);
			this.analyzerService = new AnalyzerService(this);
			this.plannerService = new PlannerService(this);
			this.executorService = new ExecutorService(this, controllerFactory);
			this.mobilityService = new MobilityService(this);

		} catch (final Exception e) {
			log.messageInfo("Incorrect arguments: some parameters for Managing Agent are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected void validateAgentArguments() {
		if (properties.getConfig().getSystemQualityThreshold() == 0 ||
				!DoubleRange.of(0, 1).contains(properties.getConfig().getSystemQualityThreshold())) {
			log.messageInfo("Incorrect argument: System quality must be from a range (0,1]");
			doDelete();
		}
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return List.of(
				new ReadAdaptationGoals(this),
				new DisableAdaptationActions(this, properties.getConfig().getDisabledActions())
		);
	}

	public MonitoringService monitor() {
		return monitoringService;
	}

	public AnalyzerService analyze() {
		return analyzerService;
	}

	public PlannerService plan() {
		return plannerService;
	}

	public ExecutorService execute() {
		return executorService;
	}

	public MobilityService move() {
		return mobilityService;
	}
}
