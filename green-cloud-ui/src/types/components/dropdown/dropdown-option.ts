import { Agent, ClientAgentStatus } from 'types/agents'
import { AgentType } from 'types/enum'

export interface DropdownOption {
   value: Agent | ClientAgentStatus | AgentType | null | string | number
   label: string
   isSelected?: boolean
}
