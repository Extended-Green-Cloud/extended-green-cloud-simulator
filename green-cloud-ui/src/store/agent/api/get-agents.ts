import axios from 'axios'
import { call, delay, put } from 'redux-saga/effects'
import { handleConnectionError } from 'store/common'
import { agentsActions } from '../actions'
import { MenuTab } from '@types'
import { cloudNetworkActions } from 'store/cloud-network'

/**
 * Method retrieves agents from backend
 *
 */
export function* fetchAgentsState() {
   while (true) {
      try {
         const { data } = yield call(() =>
            axios.get(process.env.REACT_APP_WEB_SOCKET_AGENTS_FRONTEND_URL + '/agents', {
               timeout: 2000
            })
         )
         yield put(agentsActions.setAgents(data.agents))
         yield put(cloudNetworkActions.openServerConnection(MenuTab.AGENTS))

         yield delay(200)
      } catch (err: any) {
         handleConnectionError(err, 'Agents', MenuTab.AGENTS)
      }
   }
}

/**
 * Method retrieves agents from backend
 *
 */
export function* fetchOnceAgentsState() {
   try {
      const { data } = yield call(() =>
         axios.get(process.env.REACT_APP_WEB_SOCKET_AGENTS_FRONTEND_URL + '/agents', {
            timeout: 2000
         })
      )
      yield put(agentsActions.setAgents(data.agents))
      yield put(cloudNetworkActions.openServerConnection(MenuTab.AGENTS))
   } catch (err: any) {
      handleConnectionError(err, 'Agents', MenuTab.AGENTS)
   }
}
