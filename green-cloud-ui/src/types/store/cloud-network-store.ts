import { Agent } from "../agents/agent-interface";

export type CloudNetworkStore = {
    agents: Agent[],
    currClientsNo: number;
    currActiveJobsNo: number;
    currPlannedJobsNo: number;
    finishedJobsNo: number;
    failedJobsNo: number;
    totalPrice: number;
}