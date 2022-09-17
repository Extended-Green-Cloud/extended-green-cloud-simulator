import { AgentType } from "./agent-type-enum"

export interface CommonAgentInterface {
    type: AgentType
    name: string
}

export interface CommonAgentNodeInterface {
    id: string
    label: string,
    type: AgentType
}