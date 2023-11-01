import { JobStep } from 'types/job'
import { ResourceMap } from 'types/resources'

export interface JobCreator {
   selectionPreference: string
   processorName: string
   resources: ResourceMap
   deadline: number
   duration: number
   steps: JobStep[]
}
