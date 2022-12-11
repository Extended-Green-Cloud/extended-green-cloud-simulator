import { CommonNetworkAgentInterface } from './common/common-network-agent'
import { Location } from '../location/location'
import { EnergyType } from 'types/enum'

export interface GreenEnergyAgent extends CommonNetworkAgentInterface {
   monitoringAgent: string
   serverAgent: string
   connectedServers: string[]
   agentLocation: Location
   energyType: EnergyType
   successRatio: number
   weatherPredictionError: number
}
