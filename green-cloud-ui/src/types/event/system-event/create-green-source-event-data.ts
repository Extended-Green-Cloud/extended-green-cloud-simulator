import { GreenSourceCreator } from 'types/creator'
import { CommonAgentEventData } from './common-agent-event-data'

export interface CreateGreenSourceEventData extends CommonAgentEventData {
   greenSourceData: GreenSourceCreator
}
