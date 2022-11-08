import { JobStatus } from 'types/enum/job-status-enum'
import { CommonAgentInterface } from './common/common-agent'

export interface ClientAgent extends CommonAgentInterface {
   jobId: string
   power: string
   start: string
   end: string
   jobStatusEnum: JobStatus
}
