import { ClientAgent } from "../client-agent-interface";
import { CloudNetworkAgent } from "../cloud-network-agent-interface";
import { GreenEnergyAgent } from "../green-energy-agent-interface";
import { MonitoringAgent } from "../monitoring-agent-interface";
import { ServerAgent } from "../server-agent-interface";

export type AgentNodeInterface = (CloudNetworkAgent | ClientAgent | GreenEnergyAgent | MonitoringAgent | ServerAgent)