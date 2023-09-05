import { JobStep } from './job-step'

export interface Job {
   jobId: string
   processorName: string
   cpu: number
   memory: number
   storage: number
   start: string
   end: string
   deadline: string
   duration: string
   steps: JobStep[]
}
