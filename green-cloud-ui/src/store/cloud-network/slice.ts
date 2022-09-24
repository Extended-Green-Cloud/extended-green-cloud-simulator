import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { MessagePayload, CloudNetworkStore } from "@types";


const INITIAL_STATE: CloudNetworkStore = {
    currClientsNo: 0,
    currActiveJobsNo: 0,
    currPlannedJobsNo: 0,
    finishedJobsNo: 0,
    failedJobsNo: 0,
    totalPrice: 0,
}

export const cloudNetworkSlice = createSlice({
    name: 'cloudNetwork',
    initialState: INITIAL_STATE,
    reducers: {
        incrementFinishedJobs(state) {
            state.finishedJobsNo++
        },
        incrementFailedJobs(state) {
            state.failedJobsNo++
        },
        updateCurrentClientNumber(state, action: PayloadAction<MessagePayload>) {
            state.currClientsNo = action.payload.data as number
        },
        updateCurrentPlannedJobsNumber(state, action: PayloadAction<MessagePayload>) {
            Object.assign(state, {...state, currPlannedJobsNo: action.payload.data as number})
        },
        updateCurrentActiveJobsNumber(state, action: PayloadAction<MessagePayload>) {
            Object.assign(state, {...state, currActiveJobsNo: action.payload.data as number})
        },
        setTotalPrice(state, action: PayloadAction<MessagePayload>) {
            Object.assign(state, {...state, totalPrice: action.payload.data as number})
        },
        resetCloudNetwork(state) {
            Object.assign(state, INITIAL_STATE)
        }
    }
})