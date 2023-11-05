import { AGENT_TYPES } from "../../../constants";
import {
	createAgentConnections,
	createNodeForAgent,
	getAgentByName,
	getAgentNodeById,
	getNodeState,
	registerAgent,
} from "../../../utils";
import { CLIENTS_STATE } from "../../clients";
import { GRAPH_STATE } from "../../graph";
import { AGENTS_STATE } from "../agents-state";
import { CloudNetworkAgent, GreenEnergyAgent } from "../types";
import { ServerAgent } from "../types/server-agent";

const handleSetClientNumber = (msg) => {
	const agent: CloudNetworkAgent | ServerAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const clientNumber = msg.data;

	if (agent) {
		agent.totalNumberOfClients = clientNumber;
	}
};

const handleSetTraffic = (msg) => {
	const agent: ServerAgent | GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.agentName);
	const traffic = msg.data;

	if (agent) {
		agent.traffic = traffic * 100;
		if (node) {
			node.state = getNodeState(agent);
		}
	}
};

const handleSetJobsCount = (msg) => {
	const agent: CloudNetworkAgent | ServerAgent | GreenEnergyAgent = getAgentByName(
		AGENTS_STATE.agents,
		msg.agentName
	);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.agentName);
	const jobsCount = msg.data;

	if (agent) {
		if (agent.type === AGENT_TYPES.SERVER || agent?.type === AGENT_TYPES.GREEN_ENERGY) {
			(agent as ServerAgent | GreenEnergyAgent).numberOfExecutedJobs = jobsCount;
		} else if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
			(agent as CloudNetworkAgent).totalNumberOfExecutedJobs = jobsCount;
		}

		if (node) {
			node.state = getNodeState(agent);
		}
	}
};

const handleSetJobsOnHold = (msg) => {
	const agent: ServerAgent | GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.agentName);

	if (agent) {
		agent.numberOfJobsOnHold = msg.data;

		if (node) {
			node.state = getNodeState(agent);
		}
	}
};

const handleSetActive = (msg) => {
	const agent: ServerAgent | GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const node = getAgentNodeById(GRAPH_STATE.nodes, msg.agentName);

	if (agent) {
		agent.isActive = msg.data;

		if (node) {
			node.state = getNodeState(agent);
		}

		GRAPH_STATE.connections.forEach((connection) => {
			if (connection.data.source === agent.name || connection.data.target === agent.name) {
				const secondAgent =
					connection.data.source === agent.name
						? getAgentByName(AGENTS_STATE.agents, connection.data.target)
						: getAgentByName(AGENTS_STATE.agents, connection.data.source);
				connection.state = agent.isActive && secondAgent.isActive ? "active" : "inactive";
			}
		});
	}
};

const handleSetSuccessRatio = (msg) => {
	const agent: ServerAgent | CloudNetworkAgent | GreenEnergyAgent = getAgentByName(
		AGENTS_STATE.agents,
		msg.agentName
	);
	const successRatio = msg.data;

	if (agent) {
		agent.successRatio = successRatio;
	}
};

const handleRemoveAgent = (msg) => {
	const agentName = msg.agentName;

	AGENTS_STATE.agents = AGENTS_STATE.agents.filter((agent) => agent.name !== agentName);
	GRAPH_STATE.nodes = GRAPH_STATE.nodes.filter((node) => node.id !== agentName);
	GRAPH_STATE.connections = GRAPH_STATE.connections.filter(
		(edge) => edge.data.target !== agentName && edge.data.source !== agentName
	);
};

const handleRegisterAgent = (msg) => {
	const agentType = msg.agentType;
	const registerData = msg.data;

	if (!getAgentByName(AGENTS_STATE.agents, registerData.name)) {
		const newAgent = registerAgent(registerData, agentType);

		if (newAgent) {
			if (agentType === AGENT_TYPES.CLIENT) {
				CLIENTS_STATE.clients.push(newAgent);
			} else {
				AGENTS_STATE.agents.push(newAgent);
				GRAPH_STATE.nodes.push(createNodeForAgent(newAgent));
				Object.assign(
					GRAPH_STATE.connections,
					GRAPH_STATE.connections.concat(createAgentConnections(newAgent))
				);
			}
		}
	}
};

export {
	handleSetJobsCount,
	handleSetClientNumber,
	handleSetJobsOnHold,
	handleSetActive,
	handleSetSuccessRatio,
	handleSetTraffic,
	handleRegisterAgent,
	handleRemoveAgent,
};
