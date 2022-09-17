import { AgentNodeInterface } from "types/agents/common/agent-node-interface";


export type CloudNetwork = {
    agents: AgentNodeInterface[]
    currClientsNo: number;
    currActiveJobsNo: number;
    currPlannedJobsNo: number;
    finishedJobsNo: number;
    failedJobsNo: number;
    totalPrice: number;
}