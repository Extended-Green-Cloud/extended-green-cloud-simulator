import { CreateClientEventData } from './create-client-event-data'
import { CreateGreenSourceEventData } from './create-green-source-event-data'
import { CreateServerEventData } from './create-server-event-data'

export type CreateAgentEventData = CreateClientEventData | CreateGreenSourceEventData | CreateServerEventData
