import { AGENT_TYPES, EVENT_TYPE, POWER_SHORTAGE_STATE } from "../constants/constants";
import { AGENTS_STATE } from "../module";
import { RegionalManagerAgent } from "../module/agents/types";
import { ServerAgent } from "../module/agents/types/server-agent";
import { PowerShortageEvent, SwitchOnOffEvent } from "../types";

const getRegionalManagerState = (regionalManager: RegionalManagerAgent) => {
	if (regionalManager.traffic > 85) return "high";
	if (regionalManager.traffic > 50) return "medium";

	return regionalManager.traffic > 0 ? "low" : "inactive";
};

const getServerState = (server: ServerAgent) => {
	const powerShortageEvent = server.events.find(
		(event) => event.type === EVENT_TYPE.POWER_SHORTAGE_EVENT
	) as PowerShortageEvent;
	const disablingEvent = server.events.find(
		(event) => event.type === EVENT_TYPE.SWITCH_ON_OFF_EVENT
	) as SwitchOnOffEvent;

	if (server.numberOfJobsOnHold > 0) return "on_hold";
	if (server.backUpTraffic > 0) return "back_up";
	if (powerShortageEvent.state === POWER_SHORTAGE_STATE.INACTIVE || !disablingEvent.isServerOn) return "disabled";

	return server.isActive ? "active" : "inactive";
};

const getGreenEnergyState = (greenEnergy) => {
	if (greenEnergy.numberOfJobsOnHold > 0 && greenEnergy.numberOfExecutedJobs > 0) return "on_hold";

	return greenEnergy.isActive ? "active" : "inactive";
};

const getNodeState = (agent) => {
	switch (agent.type) {
		case AGENT_TYPES.REGIONAL_MANAGER:
			return getRegionalManagerState(agent);
		case AGENT_TYPES.GREEN_ENERGY:
			return getGreenEnergyState(agent);
		case AGENT_TYPES.SERVER:
			return getServerState(agent);
		default:
			return null;
	}
};

const createRegionalManagerEdges = (agent) => {
	const centralManager = AGENTS_STATE.agents.find((agent) => agent.type === AGENT_TYPES.CENTRAL_MANAGER);
	const centralManagerEdge = createEdge(agent.name, centralManager.name);

	return [centralManagerEdge];
};

const createServerEdges = (agent) => {
	const regionalManagerEdge = createEdge(agent.name, agent.regionalManagerAgent);

	return [regionalManagerEdge];
};

const createGreenEnergyEdges = (agent) => {
	const edgeMonitoring = createEdge(agent.name, agent.monitoringAgent);
	const edgesServers = agent.connectedServers.map((server) => createEdge(agent.name, server));

	return edgesServers.concat(edgeMonitoring);
};

const createEdge = (source, target) => {
	const id = [source, target, "BI"].join("-");
	return { data: { id, source, target }, state: "inactive" };
};

const createNodeForAgent = (agent) => {
	const node = {
		id: agent.name,
		label: agent.name,
		type: agent.type,
		adaptation: agent.adaptation,
	};
	switch (agent.type) {
		case AGENT_TYPES.REGIONAL_MANAGER:
		case AGENT_TYPES.GREEN_ENERGY:
		case AGENT_TYPES.SERVER:
			return { state: "inactive", ...node };
		default:
			return node;
	}
};

const createAgentConnections = (agent) => {
	switch (agent.type) {
		case AGENT_TYPES.SERVER:
			return createServerEdges(agent);
		case AGENT_TYPES.GREEN_ENERGY:
			return createGreenEnergyEdges(agent);
		case AGENT_TYPES.REGIONAL_MANAGER:
			return createRegionalManagerEdges(agent);
		default:
			return [];
	}
};

export {
	getRegionalManagerState,
	getGreenEnergyState,
	getServerState,
	getNodeState,
	createRegionalManagerEdges,
	createServerEdges,
	createGreenEnergyEdges,
	createEdge,
	createNodeForAgent,
	createAgentConnections,
};
