import { GraphEdge } from "types/graph";
import { Agent } from "../agents/agent-interface";

export type AgentStore = {
    agents: Agent[],
    clients: Agent[],
    connections: GraphEdge[],
    selectedAgent: string | null
}