import { EnergyType } from 'types/enum'

export interface GreenSourceCreator {
   name: string
   server: string
   latitude: number
   longitude: number
   pricePerPowerUnit: number
   weatherPredictionError: number
   maximumCapacity: number
   energyType: EnergyType
}
