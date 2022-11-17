import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { ManagingSystemStore } from '@types'

const INITIAL_STATE: ManagingSystemStore = {
   systemIndicator: 0,
   goalQualityIndicators: [],
   performedAdaptations: 0,
   weakAdaptations: 0,
   strongAdaptations: 0,
   adaptationLogs: [],
   adaptationGoals: [],
}

/**
 * Slice storing current managing system data
 */
export const managingSystemSlice = createSlice({
   name: 'managingSystem',
   initialState: INITIAL_STATE,
   reducers: {
      setAdaptationData(state, action: PayloadAction<ManagingSystemStore>) {
         Object.assign(state, action.payload)
      },
      resetAdaptationData(state) {
         Object.assign(state, INITIAL_STATE)
      },
   },
})
