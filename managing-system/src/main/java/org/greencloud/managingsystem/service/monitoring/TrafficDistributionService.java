package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;

import jade.core.AID;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractGoalService {

	public static final GoalEnum GOAL = DISTRIBUTE_TRAFFIC_EVENLY;

	public static final int AGGREGATION_SIZE = 3;

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@Override
	public double readCurrentGoalQuality(int time) {
		//CNAs
		List<String> CNAs = findCNAs();
		List<AgentData> cloudNetworkMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
				.readMultipleRowsMonitoringDataForDataTypeAndAID(CLOUD_NETWORK_MONITORING, CNAs, AGGREGATION_SIZE)
				.stream()
				.toList();
		if (cloudNetworkMonitoringData.size() < AGGREGATION_SIZE * CNAs.size()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		}
		return computeGoalQualityForComponent(cloudNetworkMonitoringData);
	}

	public boolean evaluateServerTrafficDistribution() {
		double currentGoalQuality = readCurrentGoalQuality(MONITOR_SYSTEM_DATA_TIME_PERIOD);
		if (currentGoalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			return false;
		}
		return !(currentGoalQuality > 0.7);
	}

	private double computeCoefficient(List<Double> traffic) {
		int n = traffic.size();
		OptionalDouble avg = traffic.stream().mapToDouble(Double::doubleValue).average();
		double sum = 0;
		for (var data : traffic) {
			sum += Math.pow(data - avg.getAsDouble(), 2);
		}
		double sd = Math.sqrt(sum) / (n - 1);
		return sd / avg.getAsDouble();
	}

	private double computeGoalQualityForComponent(List<AgentData> data) {
		var grouppedData = data.stream()
				.collect(Collectors.groupingBy(AgentData::aid));
		List<Double> coeffs = new ArrayList<>();
		for (int i = 0; i < AGGREGATION_SIZE; i++) {
			List<Double> traffic = new ArrayList<>();
			for (var entry : grouppedData.entrySet()) {
				traffic.add(
						((CloudNetworkMonitoringData) entry.getValue().get(i).monitoringData()).getAvailablePower());
			}
			coeffs.add(computeCoefficient(traffic));
		}
		return coeffs.stream().mapToDouble(coeff -> coeff).average().getAsDouble();
	}

	private List<String> findCNAs() {
		return managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs()
				.stream()
				.map(CloudNetworkArgs::getName)
				.map(name -> new AID(name, AID.ISLOCALNAME).getName())
				.toList();
	}
}
