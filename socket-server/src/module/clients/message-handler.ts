import { JOB_STATUSES } from "../../constants/constants";
import { getAgentByName } from "../../utils/agent-utils";
import { CLIENTS_STATE, Client } from "./clients-state";

const handleSetClientJobStatus = (msg) => {
	const agent = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const jobStatus = msg.status;

	if (agent) {
		if (jobStatus === JOB_STATUSES.FAILED) {
			agent.status = jobStatus;
		}
		agent.status = jobStatus;
	}
};

const handleSetClientJobTimeFrame = (msg) => {
	const agent = getAgentByName(CLIENTS_STATE.clients, msg.agentName);
	const { start, end } = msg.data;

	if (agent) {
		agent.job.start = start;
		agent.job.end = end;
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

export {
	handleSetClientJobStatus,
	handleSetClientJobTimeFrame,
	handleSetClientJobDurationMap,
	handleUpdateJobExecutionProportion,
};
