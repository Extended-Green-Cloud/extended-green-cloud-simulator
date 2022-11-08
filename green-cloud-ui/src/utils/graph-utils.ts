import {
   Agent,
   AgentType,
   CloudNetworkAgent,
   CloudNetworkTraffic,
   GraphEdge,
   AgentNode,
   GreenEnergyAgent,
   GreenEnergyState,
   ServerAgent,
   ServerState,
} from '@types'
import {
   EDGE_UNIDIRECTED_ACTIVE,
   EDGE_UNIDIRECTED_INACTIVE,
} from 'components/graph/graph-config'
import Cytoscape from 'cytoscape'

let core: Cytoscape.Core

export const setCore = (newCore: Cytoscape.Core) => (core = newCore)

/**
 * Method creates an agent node from given agent
 *
 * @param {Agent}[agent] - agent for which the node is to be created
 *
 * @returns AgentNode
 */
export const createNodeForAgent = (agent: Agent): AgentNode => {
   const node = { id: agent.name, label: agent.name, type: agent.type }
   switch (agent.type) {
      case AgentType.CLOUD_NETWORK:
         return {
            traffic: getCloudNetworkState(agent as CloudNetworkAgent),
            ...node,
         }
      case AgentType.SERVER:
         return { state: getServerState(agent as ServerAgent), ...node }
      case AgentType.GREEN_ENERGY:
         return {
            state: getGreenEnergyState(agent as GreenEnergyAgent),
            ...node,
         }
      case AgentType.MONITORING:
         return node
      default:
         return node
   }
}

/**
 * Method selects from the set of nodes, the ones that can be constructed (based on current agents connected to cloud network)
 *
 * @param {Agent[]}[agents] - agents that are currently present in cloud network
 * @param {GraphEdge[]}[edges] - set of all edges
 *
 * @returns GraphEdge[]
 */
export const selectExistingEdges = (agents: Agent[], edges: GraphEdge[]) =>
   edges
      .filter(
         (edge) =>
            agents.find((agent) => agent.name === edge.data.target) &&
            agents.find((agent) => agent.name === edge.data.source)
      )
      .map((edge) => {
         core
            ?.edges()
            ?.$id(edge.data.id)
            .css({ ...getEdgeStyle(edge) })
         return edge
      })

const getEdgeStyle = (edge: GraphEdge) =>
   edge.state === 'active' ? EDGE_UNIDIRECTED_ACTIVE : EDGE_UNIDIRECTED_INACTIVE

const getCloudNetworkState = (
   cloudNetwork: CloudNetworkAgent
): CloudNetworkTraffic => {
   if (cloudNetwork.traffic > 85) return CloudNetworkTraffic.HIGH
   if (cloudNetwork.traffic > 50) return CloudNetworkTraffic.MEDIUM

   return cloudNetwork.traffic > 0
      ? CloudNetworkTraffic.LOW
      : CloudNetworkTraffic.INACTIVE
}

const getServerState = (server: ServerAgent): ServerState => {
   if (server.numberOfJobsOnHold > 0) return ServerState.ON_HOLD
   if (server.backUpTraffic > 0) return ServerState.BACK_UP

   return server.isActive ? ServerState.ACTIVE : ServerState.INACTIVE
}

const getGreenEnergyState = (
   greenEnergy: GreenEnergyAgent
): GreenEnergyState => {
   if (
      greenEnergy.numberOfJobsOnHold > 0 &&
      greenEnergy.numberOfExecutedJobs > 0
   )
      return GreenEnergyState.ON_HOLD

   return greenEnergy.isActive
      ? GreenEnergyState.ACTIVE
      : GreenEnergyState.INACTIVE
}
