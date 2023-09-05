import { CloudNetworkAgent } from "./cloud-network-agent";
import { GreenEnergyAgent } from "./green-energy-agent";
import { MonitoringAgent } from "./monitoring-agent";
import { SchedulerAgent } from "./scheduler-agent";
import { ServerAgent } from "./server-agent";

export type Agent = CloudNetworkAgent | GreenEnergyAgent | MonitoringAgent | ServerAgent | SchedulerAgent;
