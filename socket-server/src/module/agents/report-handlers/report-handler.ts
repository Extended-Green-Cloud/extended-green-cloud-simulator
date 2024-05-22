import { AGENT_TYPES, EVENT_TYPE } from "../../../constants";
import { getCurrentTime } from "../../../utils";
import { AGENTS_REPORTS_STATE, AGENTS_STATE } from "../agents-state";
import { RegionalManagerAgent, GreenEnergyAgent, CentralManagerAgent } from "../types";
import { ServerAgent } from "../types/server-agent";
import { reportRegionalManagerData } from "./report-handler-cloud-network";
import { reportGreenSourceData } from "./report-handler-green-energy-source";
import { reportCentralManagerData } from "./report-handler-central-manager";
import { reportServerData } from "./report-handler-server";

const changeRegionalManagerCapacityEvent = (rmaName, serverName, cpu, isAdded, isNew) => {
	const events = AGENTS_REPORTS_STATE.agentsReports.filter((agentReport) => agentReport.name === rmaName)[0]?.events;

	if (events) {
		const eventName = isAdded ? (isNew ? "New Server" : "Server enabled") : "Server disabled";
		const event = isAdded ? (isNew ? `added to ${rmaName}` : `enabled for ${rmaName}`) : `disabled from ${rmaName}`;
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
		if (agent.type === AGENT_TYPES.REGIONAL_MANAGER) {
			reportRegionalManagerData(agent as RegionalManagerAgent, time);
		} else if (agent.type === AGENT_TYPES.SERVER) {
			reportServerData(agent as ServerAgent, time);
		} else if (agent.type === AGENT_TYPES.GREEN_ENERGY) {
			reportGreenSourceData(agent as GreenEnergyAgent, time);
		} else if (agent.type === AGENT_TYPES.CENTRAL_MANAGER) {
			reportCentralManagerData(agent as CentralManagerAgent, time);
		}
	});
};

export { changeRegionalManagerCapacityEvent, updateAgentsReportsState };
