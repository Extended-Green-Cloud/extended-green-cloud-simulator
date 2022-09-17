import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { SocketConnection } from "@types";

const INITIAL_STATE: SocketConnection = {
    isConnected: false
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