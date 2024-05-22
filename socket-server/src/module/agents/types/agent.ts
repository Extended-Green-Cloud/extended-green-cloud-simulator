import { RegionalManagerAgent } from "./cloud-network-agent";
import { GreenEnergyAgent } from "./green-energy-agent";
import { MonitoringAgent } from "./monitoring-agent";
import { CentralManagerAgent } from "./central-manager-agent";
import { ServerAgent } from "./server-agent";

export type Agent = RegionalManagerAgent | GreenEnergyAgent | MonitoringAgent | ServerAgent | CentralManagerAgent;
