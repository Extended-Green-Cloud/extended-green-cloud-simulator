import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { ClientAgent, ClientAgentStatus, ClientStoreState } from '@types'

const INITIAL_STATE: ClientStoreState = {
   clients: [],
   clientData: null,
   selectedClient: null
}

/**
 * Slice storing current state of cloud network clients
 */
export const clientSlice = createSlice({
   name: 'clients',
   initialState: INITIAL_STATE,
   reducers: {
      setClients(state, action: PayloadAction<ClientAgentStatus[]>) {
         Object.assign(state, { ...state, clients: action.payload })
      },
      setClientData(state, action: PayloadAction<ClientAgent>) {
         Object.assign(state, { ...state, clientData: action.payload })
      },
      setSelectedClient(state, action: PayloadAction<string | null>) {
         state.selectedClient = action.payload
      },
      resetClients(state) {
         Object.assign(state, INITIAL_STATE)
      }
   }
})
