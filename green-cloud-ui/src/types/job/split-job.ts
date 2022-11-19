import { JobStatus } from 'types/enum'

export interface SplitJob {
   splitJobId: string
   power: string
   start: number
   end: number
   status: JobStatus
}
