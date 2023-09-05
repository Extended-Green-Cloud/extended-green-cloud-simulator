import { getAgentByName, getAgentNodeById, getAgentsByName, getNodeState } from "../../../utils";
import { GRAPH_STATE } from "../../graph";
import { AGENTS_STATE } from "../agents-state";
import { changeCloudNetworkCapacityEvent } from "../report-handlers/report-handler";
import { CloudNetworkAgent } from "../types";
import { ServerAgent } from "../types/server-agent";

const getNewTraffic = (maxCpu, cpuInUse) => (maxCpu === 0 ? 0 : (cpuInUse / maxCpu) * 100);

const getNewCloudNetworkTraffic = (agent: CloudNetworkAgent, totalCpuInUse: number) => {
	agent.isActive = totalCpuInUse > 0;
	agent.traffic = getNewTraffic(agent.maxCpuInServers, totalCpuInUse);

	const connection = GRAPH_STATE.connections.find((el) => el.data.source === agent.name);
	const node = getAgentNodeById(GRAPH_STATE.nodes, agent.name);
	node.state = getNodeState(agent);

	if (connection) {
		connection.state = agent.isActive ? "active" : "inactive";
	}
};

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
	const agent: ServerAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const resources = msg.resources;

	if (agent) {
		agent.inUseCpu = resources.cpu;
		agent.inUseMemory = resources.memory;
		agent.inUseStorage = resources.storage;
		agent.powerConsumption = msg.powerConsumption;
		agent.powerConsumptionBackUp = msg.powerConsumptionBackUp;
	}
	const cna = getAgentByName(AGENTS_STATE.agents, (agent as ServerAgent).cloudNetworkAgent);
	const totalCpuInUse = getAgentsByName(AGENTS_STATE.agents, cna.serverAgents).reduce((prev, server: ServerAgent) => {
		return server.inUseCpu + prev;
	}, 0);
	getNewCloudNetworkTraffic(cna, totalCpuInUse);
};

const handleServerDisabling = (msg) => changeCloudNetworkCapacityEvent(msg.cna, msg.server, msg.cpu, false, false);
const handleServerEnabling = (msg) => changeCloudNetworkCapacityEvent(msg.cna, msg.server, msg.cpu, true, false);

export { handleSetBackUpTraffic, handleUpdateResources, handleServerDisabling, handleServerEnabling };
