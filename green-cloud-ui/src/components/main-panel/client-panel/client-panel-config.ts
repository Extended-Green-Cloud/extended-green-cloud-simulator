import { DropdownOption, JobStatus } from '@types'
import { mapIValueWithUnit } from 'utils/resource-utils'

export const CLIENT_STATISTICS_HEADER = [
   { key: 'status', label: 'Job status' },
   { key: 'processorName', label: 'Job type' }
]

export const CLIENT_STATISTICS_RESOURCES_MAPPER = [{ label: 'Required amount', mapper: mapIValueWithUnit }]

export const CLIENT_STATISTICS_RESOURCES = [{ key: 'resources', label: '-' }]

export const CLIENT_STATISTICS_TIMELINE = [
   { key: 'duration', label: 'Initial estimated job execution duration' },
   { key: 'deadline', label: 'Job execution deadline' }
]

export const CLIENT_STATISTICS_EXECUTION = [
   { key: 'executor', label: 'Server executing job' },
   { key: 'expectedExecutionTime', label: 'Expected job execution duration' },
   { key: 'finalEndTime', label: 'Job execution time finish' },
   { key: 'estimatedPrice', label: 'Estimated job execution price' },
   { key: 'finalPrice', label: 'Final job execution price' },
   { key: 'steps', label: 'Job steps' },
   { key: 'durationMap', label: 'Job status duration map' },
   { key: 'jobExecutionProportion', label: 'Job execution percentage' }
]

export const CLIENTS_ORDER = [
   JobStatus.CREATED.toString(),
   JobStatus.PROCESSED.toString(),
   JobStatus.IN_PROGRESS.toString(),
   JobStatus.IN_PROGRESS_CLOUD.toString(),
   JobStatus.ON_BACK_UP.toString(),
   JobStatus.ON_HOLD.toString(),
   JobStatus.DELAYED.toString(),
   JobStatus.REJECTED.toString(),
   JobStatus.FINISHED.toString(),
   JobStatus.FAILED.toString(),
   JobStatus.SCHEDULED.toString()
]

type StatusColor = { [key in JobStatus]: string }

export const STATUS_COLOR: StatusColor = {
   [JobStatus.CREATED]: 'var(--gray-2)',
   [JobStatus.PROCESSED]: 'var(--gray-2)',
   [JobStatus.ACCEPTED]: 'var(--gray-2)',
   [JobStatus.IN_PROGRESS]: 'var(--green-1)',
   [JobStatus.IN_PROGRESS_CLOUD]: 'var(--green-1)',
   [JobStatus.ON_BACK_UP]: 'var(--green-1)',
   [JobStatus.ON_HOLD]: 'var(--gray-2)',
   [JobStatus.DELAYED]: 'var(--red-1)',
   [JobStatus.REJECTED]: 'var(--red-1)',
   [JobStatus.FINISHED]: 'var(--gray-2)',
   [JobStatus.FAILED]: 'var(--red-1)',
   [JobStatus.SCHEDULED]: 'var(--gray-2)'
}

export const ALL_STATUS: DropdownOption = {
   value: 'SELECT ALL STATUSES',
   label: 'SELECT ALL STATUSES',
   isSelected: true
}

export type ClientMapType = 'HEADER' | 'RESOURCES' | 'TIMELINE' | 'EXECUTION INFO'
type ClientStatisticsMap = { [key in ClientMapType]: any[] }

export const CLIENT_STATISTIC_MAPS: ClientStatisticsMap = {
   ['HEADER']: CLIENT_STATISTICS_HEADER,
   ['RESOURCES']: CLIENT_STATISTICS_RESOURCES,
   ['TIMELINE']: CLIENT_STATISTICS_TIMELINE,
   ['EXECUTION INFO']: CLIENT_STATISTICS_EXECUTION
}

export const JOB_STATUS_MAP = Object.values(JobStatus).map((key) => {
   return { value: key as string, label: key as string, isSelected: true }
})
