import { JobCreator } from 'types/creator'
import { CommonAgentEventData } from './common-agent-event-data'

export interface CreateClientEventData extends CommonAgentEventData {
   jobData: JobCreator
}
