import { configureStore } from '@reduxjs/toolkit'
import createSagaMiddleware from 'redux-saga'
import { cloudNetworkSlice } from './cloud-network'
import rootSagas from './sagas'
import { crashMiddleware, loggingMiddleware } from '@middleware'
import { agentSlice } from './agent'

const sagaMiddleware = createSagaMiddleware()

/**
 * Application store
 */
export const store = configureStore({
   reducer: {
      cloudNetwork: cloudNetworkSlice.reducer,
      agents: agentSlice.reducer,
   },
   middleware: (getDefaultMiddleware) => {
      return getDefaultMiddleware({ thunk: false }).concat([
         crashMiddleware,
         sagaMiddleware,
         loggingMiddleware,
      ])
   },
})

/**
 * Type of the root store
 */
export type RootState = ReturnType<typeof store.getState>

/**
 * Type of the store dispatch
 */
export type AppDispatch = typeof store.dispatch

sagaMiddleware.run(rootSagas)
