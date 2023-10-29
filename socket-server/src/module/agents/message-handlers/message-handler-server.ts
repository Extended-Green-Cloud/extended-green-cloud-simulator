import { AGENT_TYPES, EVENT_TYPE } from "../../../constants";
import { ServerMaintenanceEvent, SwitchOnOffEvent } from "../../../types";
import { getAgentByName, getAgentNodeById, getAgentsByName, getNodeState, mapServerResources } from "../../../utils";
import { GRAPH_STATE } from "../../graph";
import { AGENTS_STATE } from "../agents-state";
import { changeCloudNetworkCapacityEvent } from "../report-handlers/report-handler";
import { CloudNetworkAgent } from "../types";
import { ServerAgent } from "../types/server-agent";

const getNewTraffic = (maxCpu, cpuInUse) => (maxCpu === 0 ? 0 : (cpuInUse / maxCpu) * 100);

const handleSetBackUpTraffic = (msg) => {
	const agent: ServerAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.agentName);
	const backUpCpuUsage = msg.data;

	if (agent) {
		agent.backUpTraffic = backUpCpuUsage * 100;
		if (node) {
			node.state = getNodeState(agent);
		}
	}
};

const handleUpdateResources = (msg) => {
	const foundAgent: ServerAgent | CloudNetworkAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const resources = msg.resources;

	if (foundAgent.type === AGENT_TYPES.CLOUD_NETWORK) {
		foundAgent.inUseResources = mapServerResources(resources);

		const totalCpu = foundAgent.inUseResources["cpu"]?.characteristics?.["amount"]?.value;
		foundAgent.isActive = totalCpu > 0;
		foundAgent.traffic = getNewTraffic((foundAgent as CloudNetworkAgent).maxCpuInServers, totalCpu);

		const connection = GRAPH_STATE.connections.find((el) => el.data.source === foundAgent.name);
		const node = getAgentNodeById(GRAPH_STATE.nodes, foundAgent.name);
		node.state = getNodeState(foundAgent);

		if (connection) {
			connection.state = foundAgent.isActive ? "active" : "inactive";
		}
		return;
	}

	const agent = foundAgent as ServerAgent;

	if (agent) {
		agent.inUseResources = mapServerResources(resources);
		agent.powerConsumption = msg.powerConsumption;
		agent.powerConsumptionBackUp = msg.powerConsumptionBackUp;
	}
};

const handleUpdateDefaultResources = (msg) => {
	const agent: ServerAgent | CloudNetworkAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const resources = msg.resources;

	if (agent) {
		agent.resources = mapServerResources(resources);
	}
};

const handleUpdateServerMaintenanceState = (msg) => {
	const agent: ServerAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const maintenanceEvent = agent.events.find(
		(event) => event.type === EVENT_TYPE.SERVER_MAINTENANCE_EVENT
	) as ServerMaintenanceEvent;
	const { state, result } = msg;

	maintenanceEvent[state] = result;

	if (msg?.error) {
		maintenanceEvent.hasError = true;
	}
};

const handleServerDisabling = (msg) => {
	const agent: ServerAgent = getAgentByName(AGENTS_STATE.agents, msg.server);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.server);
	const switchingEvent = agent.events.find(
		(event) => event.type === EVENT_TYPE.SWITCH_ON_OFF_EVENT
	) as SwitchOnOffEvent;

	if (switchingEvent.disabled) {
		switchingEvent.disabled = false;
		switchingEvent.isServerOn = false;
	}
	node.state = getNodeState(agent);
	changeCloudNetworkCapacityEvent(msg.cna, msg.server, msg.cpu, false, false);
};
const handleServerEnabling = (msg) => {
	const agent: ServerAgent = getAgentByName(AGENTS_STATE.agents, msg.server);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.server);
	const switchingEvent = agent.events.find(
		(event) => event.type === EVENT_TYPE.SWITCH_ON_OFF_EVENT
	) as SwitchOnOffEvent;

	if (switchingEvent.disabled) {
		switchingEvent.disabled = false;
		switchingEvent.isServerOn = true;
	}
	node.state = getNodeState(agent);
	changeCloudNetworkCapacityEvent(msg.cna, msg.server, msg.cpu, true, false);
};

export {
	handleSetBackUpTraffic,
	handleUpdateResources,
	handleServerDisabling,
	handleServerEnabling,
	handleUpdateDefaultResources,
	handleUpdateServerMaintenanceState,
};
