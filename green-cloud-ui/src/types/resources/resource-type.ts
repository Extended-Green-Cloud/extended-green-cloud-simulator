import { ResourceCharacteristic } from './resource-characteristic-type'

export interface Resource {
   characteristics: { [key: string]: ResourceCharacteristic }
}
