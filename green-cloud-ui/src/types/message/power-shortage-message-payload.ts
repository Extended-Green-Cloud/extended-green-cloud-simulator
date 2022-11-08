import { EventType } from 'types/enum'
import { PowerShortageEvent } from 'types/event'

export interface PowerShortageMessage {
   agentName: string
   type: EventType
   data: PowerShortageEvent | null
}
