import { ResourceMap } from 'types/resources'
import { CommonNetworkAgentInterface } from './common/common-network-agent'

export interface ServerAgent extends CommonNetworkAgentInterface {
   cloudNetworkAgent: string
   greenEnergyAgents: string[]
   maxPower: number
   idlePower: number
   resources: ResourceMap
   price: number
   inUseResources: ResourceMap
   powerConsumption: number
   powerConsumptionBackUp: number
   backUpTraffic: number
   totalNumberOfClients: number
   successRatio: number
}
