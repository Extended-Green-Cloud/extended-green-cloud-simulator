import { ClientAgent } from "./client-agent";
import { CloudNetworkAgent } from "./cloud-network-agent";
import { GreenEnergyAgent } from "./green-energy-agent";
import { MonitoringAgent } from "./monitoring-agent";
import { ServerAgent } from "./server-agent";

export type Agent = (CloudNetworkAgent | ClientAgent | GreenEnergyAgent | MonitoringAgent | ServerAgent)