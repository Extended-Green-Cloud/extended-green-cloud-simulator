import { AgentEvent } from "types/event"
import { AgentType } from "../../enum/agent-type-enum"

export interface CommonAgentInterface {
    type: AgentType
    name: string,
    events: AgentEvent[],
    isActive: boolean,

}

export const DEFAULT_AGENT_START_COMMONS = {
    isActive: false,
}