import { ClientAgent } from './client-agent'
import { CloudNetworkAgent } from './cloud-network-agent'
import { CommonAgentInterface } from './common'
import { GreenEnergyAgent } from './green-energy-agent'
import { MonitoringAgent } from './monitoring-agent'
import { SchedulerAgent } from './scheduler-agent'
import { ServerAgent } from './server-agent'

export type Agent =
   | CommonAgentInterface
   | CloudNetworkAgent
   | ClientAgent
   | GreenEnergyAgent
   | MonitoringAgent
   | ServerAgent
   | SchedulerAgent
