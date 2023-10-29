import { ResourceMap } from 'types/resources'
import { CommonEventMessagePayload } from './common-event-message-payload'

export interface ServerMaintenanceMessagePayload extends CommonEventMessagePayload {
   newResources: ResourceMap
}
