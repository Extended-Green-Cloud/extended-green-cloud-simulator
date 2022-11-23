package com.greencloud.application.agents.server.behaviour.powershortage.handler;

import static com.greencloud.application.agents.server.behaviour.powershortage.handler.logs.PowerShortageServerHandlerLog.SERVER_INITIATE_JOB_RE_SUPPLY_LOG;
import static com.greencloud.application.agents.server.domain.ServerAgentConstants.SERVER_CHECK_POWER_SHORTAGE_JOBS;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.POWER_SHORTAGE_SOURCE_STATUSES;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareGreenPowerSupplyRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.powershortage.initiator.InitiateJobGreenEnergySupply;
import com.greencloud.commons.job.ClientJob;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour verifies if there are any jobs affected by the power shortage which were not transferred and tries to
 * supply them again using green energy
 */
public class HandleSourcePowerShortageJobs extends TickerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleSourcePowerShortageJobs.class);

	private final ServerAgent myServerAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param serverAgent agent executing the behaviour
	 */
	public HandleSourcePowerShortageJobs(final ServerAgent serverAgent) {
		super(serverAgent, SERVER_CHECK_POWER_SHORTAGE_JOBS);
		this.myServerAgent = serverAgent;
	}

	/**
	 * Method verifies first if there are any jobs that were affected by the source power shortage and then request in
	 * the consecutive Green Source to supply them again using green power
	 */
	@Override
	protected void onTick() {
		final Set<ClientJob> affectedJobs = myServerAgent.getServerJobs().entrySet().stream()
				.filter(entry -> POWER_SHORTAGE_SOURCE_STATUSES.contains(entry.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		affectedJobs.forEach(job -> {
			final AID greenSource = myServerAgent.getGreenSourceForJobMap().get(job.getJobId());
			if (Objects.nonNull(greenSource)) {
				MDC.put(MDC_JOB_ID, job.getJobId());
				logger.info(SERVER_INITIATE_JOB_RE_SUPPLY_LOG, job.getJobId());

				final ACLMessage supplyRequest = prepareGreenPowerSupplyRequest(job, greenSource);
				myServerAgent.addBehaviour(new InitiateJobGreenEnergySupply(myServerAgent, job, supplyRequest));
			}
		});
	}
}
