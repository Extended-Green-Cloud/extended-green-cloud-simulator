import { Agent, JobStatus } from '@types'

export const CLIENT_STATISTICS = [
   { key: 'jobId', label: 'Job identifier' },
   { key: 'status', label: 'Job status' },
   { key: 'power', label: 'Power used for job' },
   { key: 'start', label: 'Start date' },
   { key: 'end', label: 'End date' },
   { key: 'deadline', label: 'Job execution deadline' },
   { key: 'durationMap', label: 'Job status duration map' },
]

export const SPLIT_JOB_STATISTICS = [
   { key: 'status', label: 'Job status' },
   { key: 'power', label: 'Power used for job' },
   { key: 'start', label: 'Start date' },
   { key: 'end', label: 'End date' },
]

export const CLIENTS_ORDER = [
   JobStatus.CREATED.toString(),
   JobStatus.PROCESSED.toString(),
   JobStatus.IN_PROGRESS.toString(),
   JobStatus.ON_BACK_UP.toString(),
   JobStatus.ON_HOLD.toString(),
   JobStatus.DELAYED.toString(),
   JobStatus.REJECTED.toString(),
   JobStatus.FINISHED.toString(),
   JobStatus.FAILED.toString(),
]

export interface AgentOption {
   value: Agent | null
   label: string
}

export interface GroupedAgentOption {
   label: string
   options: AgentOption[]
}

export interface JobStatusSelect {
   jobStatus: string
   isSelected: boolean
}

export const convertJobStatus = (status: string) => status.replaceAll('_', ' ')

export const JOB_STATUS_MAP = Object.keys(JobStatus).map((key) => {
   return { jobStatus: convertJobStatus(key), isSelected: true }
})
