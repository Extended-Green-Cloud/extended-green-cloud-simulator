import { AgentAdaptationState, AgentType, RegionalManagerTraffic, GreenEnergyState, ServerState } from 'types/enum'

export interface AgentNode {
   id: string
   label: string
   type: AgentType
   state: RegionalManagerTraffic | GreenEnergyState | ServerState
   adaptation: AgentAdaptationState
}
