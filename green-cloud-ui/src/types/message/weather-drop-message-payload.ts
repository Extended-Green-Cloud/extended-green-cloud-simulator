import { EventType } from 'types/enum'
import { WeatherDropEvent } from 'types/event'

export interface WeatherDropMessage {
   agentName: string
   type: EventType
   data: WeatherDropEvent | null
}
