import { JobCreator } from 'types/job'
import { CommonAgentEventData } from './common-agent-event-data'

export interface CreateClientEventData extends CommonAgentEventData {
   jobData: JobCreator
}
