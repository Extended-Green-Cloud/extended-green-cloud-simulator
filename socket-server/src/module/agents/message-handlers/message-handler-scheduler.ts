import { AGENT_TYPES } from "../../../constants";
import { getAgentByName } from "../../../utils";
import { AGENTS_STATE } from "../agents-state";
import { SchedulerAgent } from "../types/scheduler-agent";

const handleUpdateDeadlinePriority = (msg) => {
	const agent: SchedulerAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);

	if (agent) {
		agent.deadlinePriority = msg.data;
	}
};

const handleUpdateCpuPriority = (msg) => {
	const agent: SchedulerAgent = getAgentByName(AGENTS_STATE.agents, msg.agentName);

	if (agent) {
		agent.cpuPriority = msg.data;
	}
};

const handleUpdateJobQueue = (msg) => {
	const agent: SchedulerAgent = AGENTS_STATE.agents.find(
		(agent) => agent.type === AGENT_TYPES.SCHEDULER
	) as SchedulerAgent;

	if (agent) {
		agent.scheduledJobs = msg.data.map((job) => ({
			clientName: job.clientName.split("@")[0],
			jobId: job.jobId,
		}));
	}
};

export { handleUpdateDeadlinePriority, handleUpdateCpuPriority, handleUpdateJobQueue };
