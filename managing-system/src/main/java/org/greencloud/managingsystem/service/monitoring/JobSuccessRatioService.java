package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.greencloud.commons.job.JobResultType.ACCEPTED;
import static com.greencloud.commons.job.JobResultType.FAILED;
import static java.util.Collections.singletonList;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.NETWORK_AGENT_DATA_TYPES;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.READ_SUCCESS_RATIO_CLIENTS_LOG;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.READ_SUCCESS_RATIO_CLIENT_NO_DATA_YET_LOG;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.READ_SUCCESS_RATIO_COMPONENTS_LOG;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.READ_SUCCESS_RATIO_NETWORK_DATA_YET_LOG;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.SUCCESS_RATIO_UNSATISFIED_CLIENT_LOG;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.SUCCESS_RATIO_UNSATISFIED_COMPONENT_LOG;

import java.util.List;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.ClientMonitoringData;
import com.database.knowledge.domain.agent.ServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.google.common.util.concurrent.AtomicDouble;
import com.greencloud.commons.job.JobStatusEnum;

/**
 * Service containing methods connected with monitoring system success ratio
 */
public class JobSuccessRatioService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(JobSuccessRatioService.class);

	private AtomicDouble jobSuccessRatio;

	public JobSuccessRatioService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.jobSuccessRatio = new AtomicDouble(0);
	}

	/**
	 * Method verifies if the job success ratio for overall job execution is withing specified boundary
	 *
	 * @return boolean indicating current overall state of job success ratio
	 */
	public boolean isClientJobSuccessRatioCorrect() {
		logger.info(READ_SUCCESS_RATIO_CLIENTS_LOG);
		final double currentSuccessRatio = readClientJobSuccessRatio();
		jobSuccessRatio.set(currentSuccessRatio);
		if (currentSuccessRatio == -1) {
			logger.info(READ_SUCCESS_RATIO_CLIENT_NO_DATA_YET_LOG);
			return true;
		}
		if (!isSuccessRatioWithinBound(currentSuccessRatio)) {
			logger.info(SUCCESS_RATIO_UNSATISFIED_CLIENT_LOG, currentSuccessRatio);
			return false;
		}
		return true;
	}

	/**
	 * Method verifies if the success ratio of individual network components is withing specified boundary
	 *
	 * @return boolean indicating current state of job success ratio for components
	 */
	public boolean isComponentsSuccessRatioCorrect() {
		logger.info(READ_SUCCESS_RATIO_COMPONENTS_LOG);
		final List<AgentData> componentsData = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(NETWORK_AGENT_DATA_TYPES);

		if (componentsData.isEmpty()) {
			logger.info(READ_SUCCESS_RATIO_NETWORK_DATA_YET_LOG);
			return true;
		}

		return componentsData.stream()
				.allMatch(component -> {
					final double successRatio = readSuccessRatioForNetworkComponent(component);
					boolean result = successRatio == -1 || isSuccessRatioWithinBound(successRatio);
					if (!result) {
						final String name = component.aid().split("@")[0];
						logger.info(SUCCESS_RATIO_UNSATISFIED_COMPONENT_LOG, name, successRatio);
					}
					return result;
				});
	}

	public double getJobSuccessRatio() {
		return jobSuccessRatio.get();
	}

	//TODO add more components after their data retrieving will be implemented
	private double readSuccessRatioForNetworkComponent(final AgentData component) {
		return switch (component.dataType()) {
			case SERVER_MONITORING -> readServerJobSuccessRatio((ServerMonitoringData) component.monitoringData());
			default -> -1;
		};
	}

	private double readServerJobSuccessRatio(final ServerMonitoringData monitoringData) {
		final double jobsAccepted = monitoringData.getJobResultStatistics().get(ACCEPTED);
		final double jobsFailed = monitoringData.getJobResultStatistics().get(FAILED);
		return jobsAccepted == 0 ? -1 : 1 - (jobsFailed / jobsAccepted);
	}

	private double readClientJobSuccessRatio() {
		final List<ClientMonitoringData> clientsData = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(singletonList(CLIENT_MONITORING)).stream()
				.map(AgentData::monitoringData)
				.map(ClientMonitoringData.class::cast)
				.toList();

		if (clientsData.isEmpty()) {
			return -1;
		}

		final long allCount = clientsData.size();
		final long failCount = clientsData.stream()
				.filter(data -> data.getIsFinished() && data.getCurrentJobStatus().equals(JobStatusEnum.FAILED))
				.count();
		return 1 - ( (double) failCount / allCount);
	}

	private boolean isSuccessRatioWithinBound(final double successRatio) {
		final AdaptationGoal goal = managingAgent.monitor().getAdaptationGoal(MAXIMIZE_JOB_SUCCESS_RATIO);
		return goal.isAboveThreshold() ?
				successRatio >= goal.threshold() :
				successRatio <= goal.threshold();
	}
}
