import { EventState, EventType } from "types/enum"

export type PowerShortageEvent = {
    newMaximumCapacity: number
}

export const DEFAULT_POWER_SHORTAGE_EVENT = {
    state: EventState.ACTIVE,
    disabled: false,
    type: EventType.POWER_SHORTAGE_EVENT,
}