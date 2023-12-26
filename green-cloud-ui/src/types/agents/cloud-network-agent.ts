import { ResourceMap } from 'types/resources'
import { CommonAgentInterface } from './common/common-agent'

export interface RegionalManagerAgent extends CommonAgentInterface {
   serverAgents: string[]
   maximumCapacity: number
   traffic: number
   totalNumberOfClients: number
   totalNumberOfExecutedJobs: number
   successRatio: number
   inUseResources: ResourceMap
   resources: ResourceMap
}
