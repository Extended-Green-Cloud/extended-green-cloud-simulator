package org.greencloud.managingsystem.agent;

import java.util.List;

import org.greencloud.managingsystem.service.analyzer.AnalyzerService;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.PlannerService;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.commons.agent.AgentType;
import com.greencloud.commons.scenario.ScenarioStructureArgs;

import jade.core.Location;
import jade.wrapper.ContainerController;

/**
 * Abstract agent class storing data of the Managing Agent
 */
public abstract class AbstractManagingAgent extends AbstractAgent {

	protected ScenarioStructureArgs greenCloudStructure;
	protected ContainerController greenCloudController;
	protected List<Location> containersLocations;

	protected List<AdaptationGoal> adaptationGoalList;
	protected double systemQualityThreshold;

	protected MonitoringService monitoringService;
	protected AnalyzerService analyzerService;
	protected PlannerService plannerService;
	protected ExecutorService executorService;

	/**
	 * Default constructor
	 */
	protected AbstractManagingAgent() {
		super.setup();

		this.monitoringService = new MonitoringService(this);
		this.analyzerService = new AnalyzerService(this);
		this.plannerService = new PlannerService(this);
		this.executorService = new ExecutorService(this);
		agentType = AgentType.MANAGING;
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

	public double getSystemQualityThreshold() {
		return systemQualityThreshold;
	}

	public List<AdaptationGoal> getAdaptationGoalList() {
		return adaptationGoalList;
	}

	public void setAdaptationGoalList(List<AdaptationGoal> adaptationGoalList) {
		this.adaptationGoalList = adaptationGoalList;
	}

	/**
	 * @return current green cloud structure
	 */
	public ScenarioStructureArgs getGreenCloudStructure() {
		return greenCloudStructure;
	}

	/**
	 * @return green cloud controller, used to modify green cloud structure, add/remove agents
	 */
	public ContainerController getGreenCloudController() {
		return greenCloudController;
	}

	/**
	 * @return returns a container with a given name
	 */
	public Location getContainerLocations(String containerName) {
		return containersLocations.stream()
				.filter(location -> location.getName().equals(containerName))
				.findFirst()
				.orElse(null);
	}
}
