import { ScheduledJob } from 'types/job'
import { CommonAgentInterface } from './common/common-agent'

export interface SchedulerAgent extends CommonAgentInterface {
   scheduledJobs: ScheduledJob[]
   deadlinePriority: number
   cpuPriority: number
   maxQueueSize: number
}
