import { AgentNode } from 'types/agent-nodes'
import { GraphEdge } from 'types/graph'

export type GraphStore = {
   nodes: AgentNode[]
   connections: GraphEdge[]
}
