import { AGENT_TYPES, EVENT_TYPE } from "../../../constants";
import { getCurrentTime } from "../../../utils";
import { AGENTS_REPORTS_STATE, AGENTS_STATE } from "../agents-state";
import { CloudNetworkAgent, GreenEnergyAgent, SchedulerAgent } from "../types";
import { ServerAgent } from "../types/server-agent";
import { reportCloudNetworkData } from "./report-hadnler-cloud-network";
import { reportGreenSourceData } from "./report-handler-green-energy-source";
import { reportSchedulerData } from "./report-handler-scheduler";
import { reportServerData } from "./report-handler-server";

const changeCloudNetworkCapacityEvent = (cnaName, serverName, cpu, isAdded, isNew) => {
	const events = AGENTS_REPORTS_STATE.agentsReports.filter((agentReport) => agentReport.name === cnaName)[0]?.events;

	if (events) {
		const eventName = isAdded ? (isNew ? "New Server" : "Server enabled") : "Server disabled";
		const event = isAdded ? (isNew ? `added to ${cnaName}` : `enabled for ${cnaName}`) : `disabled from ${cnaName}`;
		const eventDescription = `Server ${serverName} with CPU ${cpu} was ${event}`;

		events.push({
			type: EVENT_TYPE.AGENT_CONNECTION_CHANGE,
			time: getCurrentTime(),
			name: eventName,
			description: eventDescription,
		});
	}
};

const updateAgentsReportsState = (time) => {
	AGENTS_STATE.agents.forEach((agent) => {
		if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
			reportCloudNetworkData(agent as CloudNetworkAgent, time);
		} else if (agent.type === AGENT_TYPES.SERVER) {
			reportServerData(agent as ServerAgent, time);
		} else if (agent.type === AGENT_TYPES.GREEN_ENERGY) {
			reportGreenSourceData(agent as GreenEnergyAgent, time);
		} else if (agent.type === AGENT_TYPES.SCHEDULER) {
			reportSchedulerData(agent as SchedulerAgent, time);
		}
	});
};

export { changeCloudNetworkCapacityEvent, updateAgentsReportsState };
