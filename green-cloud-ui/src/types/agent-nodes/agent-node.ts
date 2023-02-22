import { AgentAdaptationState, AgentType, CloudNetworkTraffic, GreenEnergyState, ServerState } from 'types/enum'

export interface AgentNode {
   id: string
   label: string
   type: AgentType
   state: CloudNetworkTraffic | GreenEnergyState | ServerState
   adaptation: AgentAdaptationState
}
