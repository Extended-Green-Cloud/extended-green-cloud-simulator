package org.greencloud.managingsystem.agent.behaviour.executor;

import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.VERIFY_ACTION_START_LOG;

import java.time.Instant;
import java.util.Date;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

public class VerifyAdaptationActionResult extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(VerifyAdaptationActionResult.class);

	private final ManagingAgent myManagingAgent;
	private final TimescaleDatabase databaseClient;
	private final Instant actionTimestamp;
	private final Integer adaptationActionId;
	private final AID targetAgent;
	private final Double initialGoalQuality;

	public VerifyAdaptationActionResult(Agent agent, Instant actionTimestamp, Instant verifyTime,
			String adaptationActionId, AID targetAgent, Double initialGoalQuality) {
		super(agent, new Date(verifyTime.toEpochMilli()));
		this.myManagingAgent = (ManagingAgent) agent;
		this.databaseClient = myManagingAgent.getAgentNode().getDatabaseClient();
		this.actionTimestamp = actionTimestamp;
		this.adaptationActionId = Integer.parseInt(adaptationActionId);
		this.targetAgent = targetAgent;
		this.initialGoalQuality = initialGoalQuality;
	}

	@Override
	protected void onWake() {
		AdaptationAction performedAction = databaseClient.readAdaptationAction(adaptationActionId);
		logger.info(VERIFY_ACTION_START_LOG, performedAction, targetAgent, actionTimestamp);

		// here we read again goal quality for the period from the action timestamp to now()
		// and then update the action
	}
}
