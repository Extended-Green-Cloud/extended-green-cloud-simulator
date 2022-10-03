import { Agent } from "types/agents"
import { GraphEdge } from "types/graph"
import { CloudNetworkStore } from "types/store"

export type FetchStateMessage = {
    network: CloudNetworkStore,
    agents: {
        agents: Agent[],
        connections: GraphEdge[]
    }
}