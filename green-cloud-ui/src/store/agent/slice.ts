import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { Agent, AgentStore, PowerShortageEventData, WeatherDropEventData } from '@types'
import { getAgentByName } from './api/get-agent-by-name'
import { triggerPowerShortage, triggerWeatherDrop } from './api/trigger-events'

const INITIAL_STATE: AgentStore = {
   agents: [],
   selectedAgent: null,
   agentData: null
}

/**
 * Slice storing current state of cloud network agents
 */
export const agentSlice = createSlice({
   name: 'agents',
   initialState: INITIAL_STATE,
   reducers: {
      triggerPowerShortage(state, action: PayloadAction<PowerShortageEventData>) {
         const { agentName, newMaximumCapacity } = action.payload

         if (getAgentByName(state.agents, agentName)) {
            triggerPowerShortage(agentName, newMaximumCapacity)
         }
      },
      triggerWeatherDrop(state, action: PayloadAction<WeatherDropEventData>) {
         const { agentName, duration } = action.payload

         if (getAgentByName(state.agents, agentName)) {
            triggerWeatherDrop(agentName, duration)
         }
      },
      setAgents(state, action: PayloadAction<Agent[]>) {
         Object.assign(state, { ...state, agents: action.payload })
      },
      setAgentData(state, action: PayloadAction<Agent>) {
         Object.assign(state, { ...state, agentData: action.payload })
      },
      setSelectedAgent(state, action: PayloadAction<string>) {
         Object.assign(state, { ...state, selectedAgent: action.payload })
      },
      resetAgents(state) {
         Object.assign(state, INITIAL_STATE)
      }
   }
})
