import { configureStore } from '@reduxjs/toolkit'
import { crashMiddleware, loggingMiddleware } from '@middleware'
import createSagaMiddleware from 'redux-saga';
import { cloudNetworkSlice } from './cloud-network/slice'
import socketSagas from './socket/sagas/fetch-message-saga';
import { socketSlice } from './socket/slice';
import { guiError } from './gui-error/slice';

const sagaMiddleware = createSagaMiddleware();

export const store = configureStore({
    reducer: {
        cloudNetwork: cloudNetworkSlice.reducer,
        socket: socketSlice.reducer,
        guiError: guiError.reducer
    },
    middleware: (getDefaultMiddleware) => {
        return getDefaultMiddleware({ thunk: false }).concat([
            crashMiddleware, 
            loggingMiddleware,
            sagaMiddleware
        ])
      },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

sagaMiddleware.run(socketSagas);