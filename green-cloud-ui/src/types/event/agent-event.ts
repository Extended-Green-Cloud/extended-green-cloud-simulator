import { EventState, EventType } from "types/enum";
import { PowerShortageEvent } from "./power-shortage-event";

export interface AgentEvent {
    disabled: boolean,
    state: EventState,
    type: EventType,
    occurenceTime: string | null,
    data: PowerShortageEvent | null
}