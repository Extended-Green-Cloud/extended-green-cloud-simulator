import { CommonNetworkAgentInterface } from './common/common-network-agent'

export interface ServerAgent extends CommonNetworkAgentInterface {
   cloudNetworkAgent: string
   greenEnergyAgents: string[]
   maxPower: number
   idlePower: number
   cpu: number
   memory: number
   storage: number
   price: number
   inUseCpu: number
   inUseMemory: number
   inUseStorage: number
   powerConsumption: number
   powerConsumptionBackUp: number
   backUpTraffic: number
   totalNumberOfClients: number
   successRatio: number
}
