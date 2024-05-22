import { getAgentByName } from "../../utils/agent-utils";
import { CLIENTS_STATE, Client } from "./clients-state";

const handleSetClientJobStatus = (msg) => {
	const agent = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const jobStatus = msg.status;

	if (agent) {
		agent.status = jobStatus;
	}
};

const handleSetClientJobDurationMap = (msg) => {
	const agent = getAgentByName(CLIENTS_STATE.clients, msg.agentName);

	if (agent) {
		agent.durationMap = msg.data;
	}
};

const handleUpdateJobExecutionProportion = (msg) => {
	const agent: Client = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const proportion = msg.data;

	if (agent) {
		agent.jobExecutionProportion = proportion;
	}
};

const handleUpdateJobExecutor = (msg) => {
	const agent: Client = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const { serverName } = msg;

	if (agent) {
		agent.executor = serverName;
	}
};

const handleUpdateJobFinalPrice = (msg) => {
	const agent: Client = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const { finalPrice } = msg;

	if (agent) {
		agent.finalPrice = finalPrice;
	}
};

const handleUpdateJobEstimatedPrice = (msg) => {
	const agent: Client = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const { estimatedPrice } = msg;

	if (agent) {
		agent.estimatedPrice = estimatedPrice;
	}
};

const handleUpdateJobEstimatedTime = (msg) => {
	const agent: Client = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const { estimatedTime } = msg;

	if (agent) {
		agent.expectedExecutionTime = estimatedTime;
	}
};

const handleUpdateJobFinishDate = (msg) => {
	const agent: Client = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const { finalExecutionDate } = msg;

	if (agent) {
		agent.finalEndTime = finalExecutionDate;
	}
};

export {
	handleSetClientJobStatus,
	handleSetClientJobDurationMap,
	handleUpdateJobExecutionProportion,
	handleUpdateJobExecutor,
	handleUpdateJobFinalPrice,
	handleUpdateJobEstimatedPrice,
	handleUpdateJobEstimatedTime,
	handleUpdateJobFinishDate,
};
