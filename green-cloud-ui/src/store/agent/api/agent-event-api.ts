import { AgentEvent, EventType } from "@types"

export const getEventByType = (events: AgentEvent[], type: EventType) => {
    return events.find(event => event.type === type)
}