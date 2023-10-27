import { EventType, CommonEventMessagePayload, WeatherDropMessage } from '@types'
import axios from 'axios'

/**
 * Method triggers power shortage event
 *
 * @param {string}[agentName] - name of the agent for which the event is to be triggered
 */
export const triggerPowerShortage = (agentName: string) => {
   const data: CommonEventMessagePayload = { agentName, type: EventType.POWER_SHORTAGE_EVENT }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/powerShortage', data)
      .then(() => console.log('Power shortage event triggered successfully'))
      .catch((err) => console.error('An error occured while triggering power shortage: ' + err))
}

/**
 * Method triggers event that switches the server on or off
 *
 * @param {string}[agentName] - name of the server for which the event is to be triggered
 */
export const triggerSwitchOnOffServer = (agentName: string) => {
   const data: CommonEventMessagePayload = { agentName, type: EventType.SWITCH_ON_OFF_EVENT }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/switchOnOffServer', data)
      .then(() => console.log('Switching server state triggered successfully'))
      .catch((err) => console.error('An error occured while switching server state: ' + err))
}

/**
 * Method triggers weather drop event
 *
 * @param {string}[agentName] - name of the agent for which the event is to be triggered
 * @param {number}[duration] - duration of weather drop
 */
export const triggerWeatherDrop = (agentName: string, duration: number) => {
   const data: WeatherDropMessage = {
      agentName,
      data: { duration },
      type: EventType.WEATHER_DROP_EVENT
   }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/weatherDrop', data)
      .then(() => console.log('Weather drop event triggered successfully'))
      .catch((err) => console.error('An error occured while triggering weather drop: ' + err))
}
