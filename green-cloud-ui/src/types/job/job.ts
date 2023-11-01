import { ResourceMap } from 'types/resources'
import { JobStep } from './job-step'

export interface Job {
   jobId: string
   processorName: string
   resources: ResourceMap
   start: string
   end: string
   deadline: string
   duration: string
   steps: JobStep[]
   selectionPreference: string
}
