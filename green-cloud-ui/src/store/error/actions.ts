import { RootState } from "store/store";
import { errorSlice } from "./slice";
import { ThunkAction, AnyAction } from "@reduxjs/toolkit";
import { ErrorStore } from "@types";

export const errorActions = errorSlice.actions

export const setError = (error: ErrorStore): ThunkAction<void,RootState,unknown,AnyAction> => {
    return async (dispatch, getState) => {
        dispatch(errorActions.setError(error))
    }
}
