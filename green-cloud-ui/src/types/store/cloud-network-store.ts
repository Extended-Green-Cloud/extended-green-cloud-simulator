import { SchedulerAgent } from 'types/agents/scheduler-agent'

export type CloudNetworkStore = {
   scheduler: SchedulerAgent | null
   currClientsNo: number
   currActiveJobsNo: number
   currPlannedJobsNo: number
   finishedJobsNo: number
   failedJobsNo: number
   isServerConnected?: boolean
}
