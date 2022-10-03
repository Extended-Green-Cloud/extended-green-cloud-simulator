import { EventState, EventType } from "types/enum";

export interface AgentEvent {
    disabled: boolean,
    state: EventState,
    type: EventType,
}