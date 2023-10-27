import { CommonEventMessagePayload } from './common-event-message-payload'

export interface WeatherDropMessage extends CommonEventMessagePayload {
   data: { duration: number } | null
}
