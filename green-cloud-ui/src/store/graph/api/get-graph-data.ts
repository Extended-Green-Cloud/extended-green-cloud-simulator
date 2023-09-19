import { AgentNode, MenuTab } from '@types'
import axios from 'axios'
import { call, delay, put, select } from 'redux-saga/effects'
import { fetchOnceAgentsState } from 'store/agent'
import { cloudNetworkActions } from 'store/cloud-network'
import { handleConnectionError } from 'store/common'
import { graphActions, selectNetworkNodes } from 'store/graph'

/**
 * Method retrieves graph data from backend
 *
 */
export function* fetchGraphState() {
   while (true) {
      try {
         const { data } = yield call(() =>
            axios.get(process.env.REACT_APP_WEB_SOCKET_AGENTS_FRONTEND_URL + '/graph', {
               timeout: 2000
            })
         )

         const nodes: AgentNode[] = yield select(selectNetworkNodes)

         if (nodes.length !== data.nodes.length) {
            yield call(fetchOnceAgentsState)
         }

         yield put(graphActions.setGraphData(data))
         yield put(cloudNetworkActions.openServerConnection(MenuTab.AGENTS))

         yield delay(200)
      } catch (err: any) {
         handleConnectionError(err, 'Graph', MenuTab.AGENTS)
      }
   }
}
