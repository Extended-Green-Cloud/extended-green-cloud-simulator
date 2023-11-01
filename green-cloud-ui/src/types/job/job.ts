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

export interface JobCreator {
   selectionPreference: string
   processorName: string
   resources: ResourceMap
   deadline: number
   duration: number
   steps: JobStep[]
}
