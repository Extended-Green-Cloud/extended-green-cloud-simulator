import { CommonAgentNodeInterface, GreenEnergyState } from "./common";
import { CommonNetworkAgentInterface } from "./common/common-network-agent-interface";
import { Location } from "./common/location-type";

export interface GreenEnergyAgent extends CommonNetworkAgentInterface {
    monitoringAgent: string,
    serverAgent: string,
    agentLocation: Location,
}

export interface GreenEnergyNode extends CommonAgentNodeInterface {
    state: GreenEnergyState
}