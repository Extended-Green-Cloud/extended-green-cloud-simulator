import { ResourceCharacteristic } from './resource-characteristic-type'

export type ResourceMapper = (resourceC: ResourceCharacteristic) => string
export type InUseResourceMapper = (resourceC: ResourceCharacteristic, inUseResourceC: ResourceCharacteristic) => string

export type ResourceCharacteristicDisplay = {
   label: string
   mapper: ResourceMapper | InUseResourceMapper
}
