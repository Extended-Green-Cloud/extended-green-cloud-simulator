import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { CloudNetworkStore } from '@types'
import { resetServerState } from './api'

const INITIAL_STATE: CloudNetworkStore = {
   scheduler: null,
   currClientsNo: 0,
   currActiveJobsNo: 0,
   currPlannedJobsNo: 0,
   finishedJobsNo: 0,
   failedJobsNo: 0,
   isServerConnected: false,
}

/**
 * Slice storing current state of cloud netork summary data
 */
export const cloudNetworkSlice = createSlice({
   name: 'cloudNetwork',
   initialState: INITIAL_STATE,
   reducers: {
      setNetworkData(state, action: PayloadAction<CloudNetworkStore>) {
         Object.assign(state, action.payload)
      },
      startNetworkStateFetching(state) {
         state.isServerConnected = true
      },
      finishNetworkStateFetching(state) {
         state.isServerConnected = false
      },
      resetCloudNetwork(state) {
         // eslint-disable-next-line @typescript-eslint/no-unused-vars
         const { isServerConnected, ...prevState } = INITIAL_STATE
         Object.assign(state, { ...prevState })
         if (state.isServerConnected) {
            resetServerState()
         }
      },
   },
})
