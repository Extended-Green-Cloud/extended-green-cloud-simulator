import {
   JobCreator,
   CreateClientAgentMessagePayload,
   GreenSourceCreator,
   CreateGreenSourceAgentMessagePayload,
   ServerCreator,
   CreateServerAgentMessagePayload
} from '@types'
import axios from 'axios'

/**
 * Method triggers new client creation event
 *
 * @param {JobCreator}[jobData] - data used to create client job
 */
export const createClientAgent = (jobData: JobCreator) => {
   const data: CreateClientAgentMessagePayload = { jobData }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/createClient', data)
      .then(() => console.log('Client agent created successfully'))
      .catch((err) => console.error('An error occurred while creating Client Agent: ' + err))
}

/**
 * Method triggers new green source creation event
 *
 * @param {GreenSourceCreator}[greenSourceData] - data used to create green source
 */
export const createGreenSourceAgent = (greenSourceData: GreenSourceCreator) => {
   const data: CreateGreenSourceAgentMessagePayload = { greenSourceData }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/createGreenSource', data)
      .then(() => console.log('Green Source agent created successfully'))
      .catch((err) => console.error('An error occurred while creating Green Source Agent: ' + err))
}

/**
 * Method triggers new server creation event
 *
 * @param {ServerCreator}[serverData] - data used to create server
 */
export const createServerAgent = (serverData: ServerCreator) => {
   const data: CreateServerAgentMessagePayload = { serverData }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/createServer', data)
      .then(() => console.log('Server agent created successfully'))
      .catch((err) => console.error('An error occurred while creating Server Agent: ' + err))
}
