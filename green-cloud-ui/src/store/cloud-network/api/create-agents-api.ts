import { JobCreator, CreateClientAgentMessagePayload } from '@types'
import axios from 'axios'

/**
 * Method triggers power shortage event
 *
 * @param {string}[agentName] - name of the agent for which the event is to be triggered
 */
export const createClientAgent = (jobData: JobCreator) => {
   const data: CreateClientAgentMessagePayload = { jobData }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/createClient', data)
      .then(() => console.log('Client agent created successfully'))
      .catch((err) => console.error('An error occurred while creating client agent: ' + err))
}
