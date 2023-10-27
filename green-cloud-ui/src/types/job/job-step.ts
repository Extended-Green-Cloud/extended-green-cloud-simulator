import { ResourceMap } from 'types/resources'

export interface JobStep {
   name: string
   duration: number
   requiredResources: ResourceMap
}
