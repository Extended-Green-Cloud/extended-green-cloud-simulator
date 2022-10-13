import { Agent, JobStatus } from "@types"

export const CLIENT_STATISTICS = [
    { key: 'jobId', label: 'Job identifier'},
    { key: 'jobStatusEnum', label: 'Job status'},
    { key: 'power', label: 'Power used for job'},
    { key: 'start', label: 'Start date'},
    { key: 'end', label: 'End date'},
]

export const CLIENTS_ORDER = [
    JobStatus.CREATED.toString(), 
    JobStatus.IN_PROGRESS.toString(), 
    JobStatus.ON_BACK_UP.toString(), 
    JobStatus.ON_HOLD.toString(), 
    JobStatus.DELAYED.toString(), 
    JobStatus.REJECTED.toString(), 
    JobStatus.FINISHED.toString(),
    JobStatus.FAILED.toString()
]

export interface AgentOption {
    value: Agent | null,
    label: string
}

export interface GroupedAgentOption {
    label: string,
    options: AgentOption[]
}
