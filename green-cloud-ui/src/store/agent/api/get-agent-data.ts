import axios from 'axios'
import { call, delay, put, select } from 'redux-saga/effects'
import { agentsActions } from '../actions'
import { selectChosenNetworkAgentId } from '../selectors'
/**
 * Method retrieves agent data from backend

 */
export function* getAgentData() {
   while (true) {
      const selectedAgent: string | null = yield select(selectChosenNetworkAgentId)
      if (selectedAgent) {
         try {
            const url = `/agent?name=${selectedAgent}`
            const { data } = yield call(() =>
               axios.get(process.env.REACT_APP_WEB_SOCKET_CLIENTS_FRONTEND_URL + url, {
                  timeout: 2000
               })
            )
            yield put(agentsActions.setAgentData(data))
         } catch (err: any) {
            console.error(`An error occured while fetching the agent data: ` + err)
            throw err
         }
      }
      yield delay(500)
   }
}
