import {
   JobCreator,
   CreateClientAgentMessagePayload,
   GreenSourceCreator,
   CreateGreenSourceAgentMessagePayload
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
 * @param {JobCreator}[jobData] - data used to create green source
 */
export const createGreenSourceAgent = (greenSourceData: GreenSourceCreator) => {
   const data: CreateGreenSourceAgentMessagePayload = { greenSourceData }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/createGreenSource', data)
      .then(() => console.log('Green Source agent created successfully'))
      .catch((err) => console.error('An error occurred while creating Green Source Agent: ' + err))
}
