import { ResourceMap } from 'types/resources'
import { JobStep } from './job-step'

export interface Job {
   jobId: string
   processorName: string
   resources: ResourceMap
   deadline: string
   duration: number
   steps: JobStep[]
   selectionPreference: string
}
