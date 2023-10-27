import { PowerShortageEventState } from 'types/enum'
import { CommonAgentEvent } from './common-agent-event'

export interface PowerShortageEvent extends CommonAgentEvent {
   state: PowerShortageEventState
}
