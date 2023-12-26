import { ResourceMap } from 'types/resources'

export interface ServerCreator {
   name: string
   regionalManager: string
   maxPower: number
   idlePower: number
   resources: ResourceMap
   jobProcessingLimit: number
   price: number
}
