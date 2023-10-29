import {
   EventType,
   CommonEventMessagePayload,
   WeatherDropMessage,
   ResourceMap,
   ServerMaintenanceMessagePayload
} from '@types'
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
 * Method triggers event that changes the resources of the given server
 *
 * @param {string}[agentName] - name of the server for which the event is to be triggered
 */
export const sendMaintenanceData = (agentName: string, newResources: ResourceMap) => {
   const data: ServerMaintenanceMessagePayload = { agentName, type: EventType.SERVER_MAINTENANCE_EVENT, newResources }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/serverMaintenance', data)
      .then(() => console.log('New server configuration was sent successfully'))
      .catch((err) => console.error('An error occured while sending server configuration: ' + err))
}

/**
 * Method triggers event that resets the process of server maintenance
 *
 * @param {string}[agentName] - name of the server for which the event is to be triggered
 */
export const triggerServerMaintenanceReset = (agentName: string) => {
   const data: CommonEventMessagePayload = { agentName, type: EventType.RESET_SERVER_MAINTENANCE_EVENT }
   axios
      .post(process.env.REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL + '/resetServerMaintenance', data)
      .then(() => console.log('Resetting process of server maintenance performed successfully'))
      .catch((err) => console.error('An error occurred while resetting process of server maintenance: ' + err))
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
