import { createSlice } from "@reduxjs/toolkit";
import { SocketStore } from "@types";

const INITIAL_STATE: SocketStore = {
    isConnected: false,
}

export const socketSlice = createSlice({
    name: 'socket',
    initialState: INITIAL_STATE,
    reducers: {
        connectSocket(state) {
            state.isConnected = true
        },
        openSocketConnection(state) {},
        closeSocketConnection(state) {
            state.isConnected = false
        }
    }
})