/* eslint-disable @typescript-eslint/no-unused-vars */
import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { AgentType, CloudNetworkStore, MenuTab } from '@types'
import { resetServerState } from './api'
import {
   CreateAgentEventData,
   CreateClientEventData,
   CreateGreenSourceEventData,
   CreateServerEventData
} from 'types/event'
import { createClientAgent, createGreenSourceAgent, createServerAgent } from './api/create-agents-api'

const INITIAL_STATE: CloudNetworkStore = {
   currClientsNo: 0,
   currActiveJobsNo: 0,
   currActiveInCloudJobsNo: 0,
   currPlannedJobsNo: 0,
   finishedJobsNo: 0,
   finishedJobsInCloudNo: 0,
   failedJobsNo: 0,
   isNetworkSocketConnected: null,
   isAdaptationSocketConnected: null,
   isClientSocketConnected: null,
   isAgentSocketConnected: null,
   connectionToast: true
}

/**
 * Slice storing current state of regional manager summary data
 */
export const cloudNetworkSlice = createSlice({
   name: 'cloudNetwork',
   initialState: INITIAL_STATE,
   reducers: {
      setNetworkData(state, action: PayloadAction<CloudNetworkStore>) {
         Object.assign(state, action.payload)
      },
      createAgent(state, action: PayloadAction<CreateAgentEventData>) {
         const { agentType } = action.payload
         if (agentType === AgentType.CLIENT) {
            createClientAgent((action.payload as CreateClientEventData).clientData)
         }
         if (agentType === AgentType.GREEN_ENERGY) {
            createGreenSourceAgent((action.payload as CreateGreenSourceEventData).greenSourceData)
         }
         if (agentType === AgentType.SERVER) {
            createServerAgent((action.payload as CreateServerEventData).serverData)
         }
      },
      resetServerConnection(state) {
         state.isNetworkSocketConnected = null
         state.isAdaptationSocketConnected = null
         state.isAgentSocketConnected = null
         state.isClientSocketConnected = null
         state.connectionToast = true
      },
      openServerConnection(state, action: PayloadAction<MenuTab>) {
         const tabName = action.payload

         if (tabName === MenuTab.ADAPTATION) {
            state.isAdaptationSocketConnected = true
         } else if (tabName === MenuTab.CLIENTS) {
            state.isClientSocketConnected = true
         } else if (tabName === MenuTab.CLOUD_SUMMARY) {
            state.isNetworkSocketConnected = true
         }
         state.isAgentSocketConnected = true
         state.connectionToast = true
      },
      closeServerConnection(state, action: PayloadAction<MenuTab>) {
         const tabName = action.payload

         if (tabName === MenuTab.ADAPTATION) {
            state.isAdaptationSocketConnected = false
         } else if (tabName === MenuTab.AGENTS) {
            state.isAgentSocketConnected = false
         } else if (tabName === MenuTab.CLIENTS) {
            state.isClientSocketConnected = false
         } else if (tabName === MenuTab.CLOUD_SUMMARY) {
            state.isNetworkSocketConnected = false
         }
         state.connectionToast = false
      },
      resetCloudNetwork(state) {
         const {
            isAdaptationSocketConnected,
            isAgentSocketConnected,
            isClientSocketConnected,
            isNetworkSocketConnected,
            ...prevState
         } = INITIAL_STATE
         Object.assign(state, { ...prevState })

         if (state.isAdaptationSocketConnected) {
            resetState()
         } else {
            state.isAdaptationSocketConnected = true
            state.connectionToast = true
            resetState()
         }

         if (state.isAgentSocketConnected) {
            resetState()
         } else {
            state.isAgentSocketConnected = true
            state.connectionToast = true
            resetState()
         }

         if (state.isClientSocketConnected) {
            resetState()
         } else {
            state.isClientSocketConnected = true
            state.connectionToast = true
            resetState()
         }

         if (state.isNetworkSocketConnected) {
            resetState()
         } else {
            state.isNetworkSocketConnected = true
            state.connectionToast = true
            resetState()
         }
      }
   }
})

const resetState = () => {
   resetServerState(process.env.REACT_APP_WEB_SOCKET_AGENTS_FRONTEND_URL)
   resetServerState(process.env.REACT_APP_WEB_SOCKET_CLIENTS_FRONTEND_URL)
   resetServerState(process.env.REACT_APP_WEB_SOCKET_MANAGING_FRONTEND_URL)
   resetServerState(process.env.REACT_APP_WEB_SOCKET_NETWORK_FRONTEND_URL)
   resetServerState(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL)
}
