import { CloudNetworkTraffic } from "./common";
import { CommonAgentInterface, CommonAgentNodeInterface } from "./common/common-agent-interface";

export interface CloudNetworkAgent extends CommonAgentInterface {
    serverAgents: string[],
    maximumCapacity: number,
    traffic: number,
    totalNumberOfClients: number,
    totalNumberOfExecutedJobs: number
}

export interface CloudNetworkNode extends CommonAgentNodeInterface {
    traffic: CloudNetworkTraffic
}