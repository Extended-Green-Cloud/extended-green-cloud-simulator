import { CommonAgentEvent } from './common-agent-event'

export interface SwitchOnOffEvent extends CommonAgentEvent {
   isServerOn: boolean
}
