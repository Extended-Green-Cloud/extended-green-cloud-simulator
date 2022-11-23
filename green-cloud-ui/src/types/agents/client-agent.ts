import { JobStatus } from 'types/enum/job-status-enum'
import { Job, SplitJob } from 'types/job'
import { CommonAgentInterface } from './common/common-agent'

export interface ClientAgent extends CommonAgentInterface {
   job: Job
   status: JobStatus
   isSplit: boolean
   splitJobs: SplitJob[]
}
