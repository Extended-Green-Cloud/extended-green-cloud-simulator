import { ResourceCharacteristic } from './resource-characteristic-type'

export interface Resource {
   characteristics: { [key: string]: ResourceCharacteristic }
   emptyResource: Resource | null
   resourceValidator?: string
   resourceComparator?: string
}
