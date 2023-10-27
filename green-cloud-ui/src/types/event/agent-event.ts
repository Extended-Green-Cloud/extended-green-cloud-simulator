import { CommonAgentEvent } from './common-agent-event'
import { PowerShortageEvent } from './power-shortage-event'
import { SwitchOnOffEvent } from './switch-on-off-event'

export type AgentEvent = PowerShortageEvent | CommonAgentEvent | SwitchOnOffEvent
