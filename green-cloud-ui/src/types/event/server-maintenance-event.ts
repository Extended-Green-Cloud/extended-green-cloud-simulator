import { CommonAgentEvent } from './common-agent-event'

export interface ServerMaintenanceEvent extends CommonAgentEvent {
   hasStarted: boolean
   hasError: boolean
   sendNewData: boolean | null
   processDataInServer: boolean | null
   informationInManager: boolean | null
   maintenanceCompleted: boolean | null
}
