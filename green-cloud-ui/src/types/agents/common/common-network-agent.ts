import { CommonAgentInterface } from './common-agent'

export interface CommonNetworkAgentInterface extends CommonAgentInterface {
   traffic: number
   numberOfExecutedJobs: number
   numberOfJobsOnHold: number
}
