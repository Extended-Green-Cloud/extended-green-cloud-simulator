import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { ErrorStore } from "@types";

const INITIAL_STATE: ErrorStore = {
    code: "",
    message: ""
}

export const errorSlice = createSlice({
    name: 'error',
    initialState: INITIAL_STATE,
    reducers: {
        setError(state, action: PayloadAction<ErrorStore>) {
            state = action.payload
        }
    }
})