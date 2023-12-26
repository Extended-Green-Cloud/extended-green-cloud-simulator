import { ResourceMap } from 'types/resources'
import { CommonNetworkAgentInterface } from './common/common-network-agent'
import { ServerState } from 'types/enum'

export interface ServerAgent extends CommonNetworkAgentInterface {
   regionalManagerAgent: string
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
   state: ServerState
}
