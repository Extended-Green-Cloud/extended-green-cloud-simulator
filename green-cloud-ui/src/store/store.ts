import { configureStore } from '@reduxjs/toolkit'
import createSagaMiddleware from 'redux-saga';
import { cloudNetworkSlice } from './cloud-network'
import socketSagas from './socket/sagas/sagas';
import { socketSlice } from './socket';
import { errorSlice } from './error';
import { crashMiddleware, loggingMiddleware } from '@middleware'

const sagaMiddleware = createSagaMiddleware();

export const store = configureStore({
    reducer: {
        cloudNetwork: cloudNetworkSlice.reducer,
        socket: socketSlice.reducer,
        error: errorSlice.reducer
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