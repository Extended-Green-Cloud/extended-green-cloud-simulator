package org.greencloud.managingsystem.service.common;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;

import jade.core.AID;

public class TestAdaptationPlanFactory {

	public static AbstractPlan getTestAdaptationPlan(ManagingAgent managingAgent, AID targetAid,
			AdaptationActionParameters parameters) {
		return new AbstractPlan(ADD_SERVER, managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO) {

			@Override
			public boolean isPlanExecutable() {
				return true;
			}

			@Override
			public AID getTargetAgent() {
				return targetAid;
			}

			@Override
			public AdaptationActionParameters getActionParameters() {
				return parameters;
			}

			@Override
			public AbstractPlan constructAdaptationPlan() {
				actionParameters = parameters;
				targetAgent = targetAid;
				return this;
			}
		};
	}
}

