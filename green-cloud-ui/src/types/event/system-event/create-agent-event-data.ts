import { CreateClientEventData } from './create-client-event-data'
import { CreateGreenSourceEventData } from './create-green-source-event-data'

export type CreateAgentEventData = CreateClientEventData | CreateGreenSourceEventData
