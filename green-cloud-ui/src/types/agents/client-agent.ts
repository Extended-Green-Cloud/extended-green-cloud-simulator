import { JobStatus } from 'types/enum/job-status-enum'
import { Job, JobDurationMap } from 'types/job'
import { CommonAgentInterface } from './common/common-agent'

export interface ClientAgent extends CommonAgentInterface {
   job: Job
   executor: string
   expectedExecutionTime: number
   finalEndTime: number
   estimatedPrice: number
   finalPrice: number
   status: JobStatus
   durationMap: JobDurationMap
   jobExecutionProportion: number
}
