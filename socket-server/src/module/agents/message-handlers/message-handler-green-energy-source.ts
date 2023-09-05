import { AGENT_TYPES } from "../../../constants";
import { createEdge, getAgentByName } from "../../../utils";
import { GRAPH_STATE } from "../../graph";
import { AGENTS_STATE } from "../agents-state";
import { GreenEnergyAgent } from "../types";

const handleWeatherPredictionError = (msg) => {
	const agent: GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const error = msg.data;

	if (agent) {
		agent.weatherPredictionError = error;
	}
};

const handleUpdateEnergyInUse = (msg) => {
	const agent: GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);
	const energy = msg.data;

	if (agent) {
		agent.energyInUse = energy;
	}
};

const handleUpdateGreenEnergy = (msg) => {
	const agent: GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);

	if (agent && agent.type === AGENT_TYPES.GREEN_ENERGY) {
		agent.availableGreenEnergy = msg.data;
	}
};

const handleUpdateServerConnection = (msg) => {
	const agent: GreenEnergyAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);

	if (agent) {
		const { isConnected, serverName } = msg.data;

		if (isConnected) {
			agent.connectedServers.push(serverName);
			Object.assign(GRAPH_STATE.connections, GRAPH_STATE.connections.concat(createEdge(agent.name, serverName)));
		} else {
			agent.connectedServers = agent.connectedServers.filter((server) => server !== serverName);
			GRAPH_STATE.connections = GRAPH_STATE.connections.filter(
				(edge) => edge.data.id !== [agent.name, serverName, "BI"].join("-")
			);
		}
	}
};

export { handleWeatherPredictionError, handleUpdateGreenEnergy, handleUpdateServerConnection, handleUpdateEnergyInUse };
