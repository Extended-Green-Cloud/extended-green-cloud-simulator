import { AGENT_TYPES } from "../../../constants";
import { AGENTS_STATE } from "../agents-state";
import { CentralManagerAgent } from "../types/central-manager-agent";

const handleUpdateJobQueue = (msg) => {
	const agent: CentralManagerAgent = AGENTS_STATE.agents.find(
		(agent) => agent.type === AGENT_TYPES.CENTRAL_MANAGER
	) as CentralManagerAgent;

	if (agent) {
		agent.scheduledJobs = msg.data.map((job) => ({
			clientName: job.clientName.split("@")[0],
			jobId: job.jobId,
		}));
	}
};

export { handleUpdateJobQueue };
