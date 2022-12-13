import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { FetchStateMessage, GraphStore } from '@types'

const INITIAL_STATE: GraphStore = {
   nodes: [],
   connections: [],
}

/**
 * Slice storing current state of cloud network graph
 */
export const graphSlice = createSlice({
   name: 'graph',
   initialState: INITIAL_STATE,
   reducers: {
      setGraphData(state, action: PayloadAction<FetchStateMessage>) {
         Object.assign(state, { ...state, ...action.payload })
      },
      resetGraph(state) {
         Object.assign(state, INITIAL_STATE)
      },
   },
})
