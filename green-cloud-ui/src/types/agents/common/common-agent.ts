import { GraphEdge } from "types/graph"
import { AgentType } from "../../enum/agent-type-enum"

export interface CommonAgentInterface {
    type: AgentType
    name: string
    edges?: GraphEdge[]
}