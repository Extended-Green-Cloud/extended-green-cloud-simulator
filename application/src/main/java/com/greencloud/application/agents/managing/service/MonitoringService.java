package com.greencloud.application.agents.managing.service;

import static com.greencloud.application.agents.managing.service.logs.ManagingAgentServiceLog.READ_ADAPTATION_GOALS_LOG;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.managing.AbstractManagingAgent;

/**
 * Service containing methods connected with monitoring the quality of the system
 */
public class MonitoringService extends AbstractManagingService{

	private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

	public MonitoringService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method is used to read from the database, the system's adaptation goals
	 */
	public void readSystemAdaptationGoals() {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			logger.info(READ_ADAPTATION_GOALS_LOG);
			managingAgent.setAdaptationGoalList(managingAgent.getAgentNode().getDatabaseClient().readAdaptationGoals());
		}
	}
}
