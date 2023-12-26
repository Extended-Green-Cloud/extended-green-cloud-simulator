import { ClientAgent } from './client-agent'
import { RegionalManagerAgent } from './cloud-network-agent'
import { CommonAgentInterface } from './common'
import { GreenEnergyAgent } from './green-energy-agent'
import { MonitoringAgent } from './monitoring-agent'
import { SchedulerAgent } from './scheduler-agent'
import { ServerAgent } from './server-agent'

export type Agent =
   | CommonAgentInterface
   | RegionalManagerAgent
   | ClientAgent
   | GreenEnergyAgent
   | MonitoringAgent
   | ServerAgent
   | SchedulerAgent
