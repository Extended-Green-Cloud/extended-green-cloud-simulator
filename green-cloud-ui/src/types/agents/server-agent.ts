import { CommonNetworkAgentInterface } from "./common/common-network-agent";

export interface ServerAgent extends CommonNetworkAgentInterface {
    cloudNetworkAgent: string,
    greenEnergyAgents: string[],
    backUpTraffic: number,
    totalNumberOfClients: number
}