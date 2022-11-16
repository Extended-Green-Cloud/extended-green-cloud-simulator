import { CommonAgentInterface } from './common/common-agent'

export interface SchedulerAgent extends CommonAgentInterface {
   scheduledJobs: string[]
   deadlinePriority: number
   powerPriority: number
   maxQueueSize: number
}
