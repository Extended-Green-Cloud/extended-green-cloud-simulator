import { ScheduledJob } from 'types/job'
import { CommonAgentInterface } from './common/common-agent'

export interface CentralManagerAgent extends CommonAgentInterface {
   scheduledJobs: ScheduledJob[]
   maxQueueSize: number
}
