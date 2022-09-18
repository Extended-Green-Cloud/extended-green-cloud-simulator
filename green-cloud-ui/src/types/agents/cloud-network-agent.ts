import { CommonAgentInterface } from "./common/common-agent";

export interface CloudNetworkAgent extends CommonAgentInterface {
    serverAgents: string[],
    maximumCapacity: number,
    traffic: number,
    totalNumberOfClients: number,
    totalNumberOfExecutedJobs: number
}