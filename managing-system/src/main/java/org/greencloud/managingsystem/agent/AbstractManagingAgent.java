package org.greencloud.managingsystem.agent;

import java.util.List;

import org.greencloud.managingsystem.service.AnalyzerService;
import org.greencloud.managingsystem.service.PlannerService;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.greencloud.application.agents.AbstractAgent;

/**
 * Abstract agent class storing data of the Managing Agent
 */
public abstract class AbstractManagingAgent extends AbstractAgent {

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
}
