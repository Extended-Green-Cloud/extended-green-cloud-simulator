import { CommonAgentNodeInterface, ServerState } from "./common";
import { CommonNetworkAgentInterface } from "./common/common-network-agent-interface";

export interface ServerAgent extends CommonNetworkAgentInterface {
    cloudNetworkAgent: string,
    greenEnergyAgents: string[],
    backUpTraffic: number,
    totalNumberOfClients: number
}

export interface ServerNode extends CommonAgentNodeInterface {
    state: ServerState
}