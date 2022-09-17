import { RootState } from "store/store";
import { guiError } from "./slice";
import { ThunkAction, AnyAction } from "@reduxjs/toolkit";
import { GUIError } from "@types";

export const guiErrorActions = guiError.actions

export const setError = (error: GUIError): ThunkAction<void,RootState,unknown,AnyAction> => {
    return async (dispatch, getState) => {
        dispatch(guiErrorActions.setError(error))
    }
}
