import { ResourceCharacteristic } from './resource-characteristic-type'

export interface Resource {
   characteristics: { [key: string]: ResourceCharacteristic }
   emptyResource: Resource | null
   sufficiencyValidator?: string
   resourceComparator?: string
}
