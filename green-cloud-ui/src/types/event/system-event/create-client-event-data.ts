import { ClientCreator } from 'types/creator'
import { CommonAgentEventData } from './common-agent-event-data'

export interface CreateClientEventData extends CommonAgentEventData {
   clientData: ClientCreator
}
