import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { CloudNetwork, GUIError } from "@types";

const INITIAL_STATE: GUIError = {
    code: "",
    message: ""
}

export const guiError = createSlice({
    name: 'guiError',
    initialState: INITIAL_STATE,
    reducers: {
        setError(state, action: PayloadAction<GUIError>) {
            state = action.payload
        }
    }
})