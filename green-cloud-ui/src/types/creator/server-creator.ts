import { ResourceMap } from 'types/resources'

export interface ServerCreator {
   name: string
   cloudNetwork: string
   maxPower: number
   idlePower: number
   resources: ResourceMap
   jobProcessingLimit: number
   price: number
}
