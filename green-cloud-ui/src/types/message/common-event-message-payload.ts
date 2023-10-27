import { EventType } from 'types/enum'

export interface CommonEventMessagePayload {
   agentName: string
   type: EventType
}
