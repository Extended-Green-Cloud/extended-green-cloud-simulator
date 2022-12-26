package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.JOB_DISTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.JOB_DISTRIBUTION_UNSATISFIED_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;
import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;

import jade.core.AID;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractGoalService {

	private static final Logger logger = LoggerFactory.getLogger(JobSuccessRatioService.class);
	public static final GoalEnum GOAL = DISTRIBUTE_TRAFFIC_EVENLY;

	public static final int AGGREGATION_SIZE = 3;

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@Override
	public double readCurrentGoalQuality(int time) {
		List<Double> allQualities = new ArrayList<>();

		//CNAs
		List<String> CNAs = findCNAs();
		double CNAQuality = readCNAQuality(CNAs);
		allQualities.add(CNAQuality);

		//Servers
		List<List<String>> servers = findServers(CNAs);
		List<Double> serversQuality = readServerQuality(servers);
		allQualities.addAll(serversQuality);

		// Worst quality
		OptionalDouble worstQuality = allQualities.stream().filter(q -> q != DATA_NOT_AVAILABLE_INDICATOR).mapToDouble(Double::doubleValue).min();

		if(!worstQuality.isPresent()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		}

		return worstQuality.getAsDouble();
	}

	@Override
	public boolean evaluateAndUpdate() {
		logger.info(READ_JOB_DSTRIBUTION_LOG);
		double currentGoalQuality = readCurrentGoalQuality(MONITOR_SYSTEM_DATA_TIME_PERIOD);

		if (currentGoalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			logger.info(READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET);
			return true;
		}

		logger.info(JOB_DISTRIBUTION_LOG, currentGoalQuality);
		updateGoalQuality(GOAL, currentGoalQuality);
		boolean result = managingAgent.monitor().isQualityInBounds(currentGoalQuality, DISTRIBUTE_TRAFFIC_EVENLY);

		if (!result) {
			logger.info(JOB_DISTRIBUTION_UNSATISFIED_LOG, currentGoalQuality);
		}

		return result;
	}

	@VisibleForTesting
	protected double computeCoefficient(List<Double> traffic) {
		int n = traffic.size();
		OptionalDouble avg = traffic.stream().mapToDouble(Double::doubleValue).average();
		double sum = 0;
		for (var data : traffic) {
			sum += Math.pow(data - avg.getAsDouble(), 2);
		}
		double sd = Math.sqrt(sum / (n - 1));
		return sd / avg.getAsDouble();
	}

	@VisibleForTesting
	protected double computeGoalQualityForCNA(List<AgentData> data) {
		var grouppedData = data.stream()
				.collect(Collectors.groupingBy(AgentData::aid));
		List<Double> coeffs = new ArrayList<>();
		for (int i = 0; i < grouppedData.get(data.get(0).aid()).size(); i++) {
			List<Double> traffic = new ArrayList<>();
			for (var entry : grouppedData.entrySet()) {
				traffic.add(
						((CloudNetworkMonitoringData) entry.getValue().get(i).monitoringData()).getAvailablePower());
			}
			coeffs.add(computeCoefficient(traffic));
		}
		return 1 - coeffs.stream().mapToDouble(coeff -> coeff).average().getAsDouble();
	}

	@VisibleForTesting
	protected double computeGoalQualityForServer(List<AgentData> data) {
		var grouppedData = data.stream()
				.collect(Collectors.groupingBy(AgentData::aid));
		List<Double> coeffs = new ArrayList<>();
		for (int i = 0; i < grouppedData.get(data.get(0).aid()).size(); i++) {
			List<Double> traffic = new ArrayList<>();
			for (var entry : grouppedData.entrySet()) {
				traffic.add(
						((ServerMonitoringData) entry.getValue().get(i).monitoringData()).getCurrentMaximumCapacity() -
								((ServerMonitoringData)entry.getValue().get(i).monitoringData()).getCurrentTraffic());
			}
			coeffs.add(computeCoefficient(traffic));
		}
		return 1 - coeffs.stream().mapToDouble(coeff -> coeff).average().getAsDouble();
	}

	private List<String> findCNAs() {
		return managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs()
				.stream()
				.map(CloudNetworkArgs::getName)
				.map(name -> new AID(name, AID.ISLOCALNAME).getName())
				.toList();
	}

	private List<List<String>> findServers(List<String> CNAs) {
		List<List<String>> serversList = new ArrayList<>();
		CNAs.stream().forEach(CNA -> {
			var serverList = new ArrayList<>(managingAgent.getGreenCloudStructure().getServersForCloudNetworkAgent(CNA.split("@")[0]));
			var fullNameList = serverList.stream().map(server -> new AID(server, AID.ISLOCALNAME).getName()).toList();
			serversList.add(fullNameList);
		});
		return serversList;
	}

	private double readCNAQuality(List<String> CNAs) {
		List<AgentData> cloudNetworkMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
				.readMultipleRowsMonitoringDataForDataTypeAndAID(CLOUD_NETWORK_MONITORING, CNAs, AGGREGATION_SIZE)
				.stream()
				.toList();

		double CNAQuality;
		if (cloudNetworkMonitoringData.size() < AGGREGATION_SIZE * CNAs.size()) {
			CNAQuality =  DATA_NOT_AVAILABLE_INDICATOR;
		}
		else {
			CNAQuality = computeGoalQualityForCNA(cloudNetworkMonitoringData);
		}
		return CNAQuality;
	}

	private List<Double> readServerQuality(List<List<String>> servers) {
		List<Double> serversQuality = new ArrayList<>();
		servers.stream().forEach(serversList -> {
			double quality;
			List<AgentData> serverMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
					.readMultipleRowsMonitoringDataForDataTypeAndAID(SERVER_MONITORING, serversList, AGGREGATION_SIZE)
					.stream()
					.toList();
			if (serverMonitoringData.size() < AGGREGATION_SIZE * serversList.size()) {
				quality = DATA_NOT_AVAILABLE_INDICATOR;
			}
			else {
				serverMonitoringData = serverMonitoringData
						.stream()
						.filter(data -> !((ServerMonitoringData)data.monitoringData()).isDisabled())
						.toList();
				quality = computeGoalQualityForServer(serverMonitoringData);
			}
			serversQuality.add(quality);
		});
		return serversQuality;
	}
}
