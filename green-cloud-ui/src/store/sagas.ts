import { all, call, put, race, take, delay } from 'redux-saga/effects'
import axios from 'axios'
import { agentsActions } from './agent'
import { cloudNetworkActions } from './cloud-network'
import { toast } from 'react-toastify'
import { managinSystemActions } from './managing-system'

/**
 * Saga responsible for wathich the state fetching action
 */
export function* watchFetchState() {
   while (true) {
      yield take(cloudNetworkActions.startNetworkStateFetching)
      yield race({
         fetch: call(fetchState),
         finish: take(cloudNetworkActions.finishNetworkStateFetching),
      })
   }
}

/**
 * Root store sagas
 */
export default function* rootSagas() {
   yield all([watchFetchState()])
}

function* fetchState() {
   while (true) {
      try {
         const { data } = yield call(() =>
            axios.get(process.env.REACT_APP_WEB_SOCKET_FRONTEND_URL, {
               timeout: 2000,
            })
         )
         yield all([
            put(agentsActions.setAgentsData(data.agents)),
            put(cloudNetworkActions.setNetworkData(data.network)),
            put(managinSystemActions.setAdaptationData(data.managingSystem)),
         ])
         yield delay(200)
      } catch (err: any) {
         if (err.code === 'ERR_NETWORK') {
            console.error('Server is disconnected')
            toast.dismiss()
            toast.error('Server is disconnected')
            yield put(cloudNetworkActions.finishNetworkStateFetching())
         }
         console.error('An error occured while fetching the agent data: ' + err)
      }
   }
}
