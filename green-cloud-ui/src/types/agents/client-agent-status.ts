import { JobStatus } from 'types/enum/job-status-enum'

export interface ClientAgentStatus {
   name: string
   status: JobStatus
   processorName: string
}
