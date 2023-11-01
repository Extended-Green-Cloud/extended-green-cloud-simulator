import { ServerCreator } from 'types/creator'
import { CommonAgentEventData } from './common-agent-event-data'

export interface CreateServerEventData extends CommonAgentEventData {
   serverData: ServerCreator
}
