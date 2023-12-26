import { AGENT_TYPES } from "../../../constants";
import { AGENTS_REPORTS_STATE, AGENTS_STATE } from "../agents-state";
import { SchedulerAgent } from "../types";
import { RegionalManagerAgent } from "../types/cloud-network-agent";

const reportSystemTraffic = (time) => {
	const currentState = AGENTS_STATE.agents
		.filter((agent) => agent.type === AGENT_TYPES.REGIONAL_MANAGER)
		.reduce(
			(sum, agent: RegionalManagerAgent) => {
				sum.capacity = sum.capacity + agent.maxCpuInServers;
				sum.traffic = sum.traffic + agent.maxCpuInServers * agent.traffic;
				return sum;
			},
			{ capacity: 0, traffic: 0 }
		);
	const currentTraffic = currentState.capacity === 0 ? 0 : currentState.traffic / currentState.capacity;

	return { time, value: currentTraffic };
};

const reportSchedulerData = (agent: SchedulerAgent, time) => {
	const reports = AGENTS_REPORTS_STATE.agentsReports.filter((agentReport) => agentReport.name === agent.name)[0]
		.reports;

	const queueCapacity = agent.maxQueueSize === 0 ? 0 : agent.scheduledJobs.length;

	reports["deadlinePriorityReport"].push({ time, value: agent.deadlinePriority });
	reports["cpuPriorityReport"].push({ time, value: agent.cpuPriority });
	reports["queueCapacityReport"].push({ time, value: queueCapacity });
	reports["trafficReport"].push(reportSystemTraffic(time));
};

export { reportSchedulerData };
