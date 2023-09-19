import { EventType, PowerShortageMessage, WeatherDropMessage } from '@types'
import axios from 'axios'

/**
 * Method triggers power shortage event
 *
 * @param {string}[agentName] - name of the agent for which the event is to be triggered
 * @param {number}[maxCapacity] - new maximum capacity after power shortage
 */
export const triggerPowerShortage = (agentName: string, maxCapacity: number) => {
   const data: PowerShortageMessage = {
      agentName,
      data: {
         newMaximumCapacity: maxCapacity
      },
      type: EventType.POWER_SHORTAGE_EVENT
   }

   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/powerShortage', data)
      .then(() => console.log('Power shortage event triggered successfully'))
      .catch((err) => console.error('An error occured while triggering power shortage: ' + err))
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
