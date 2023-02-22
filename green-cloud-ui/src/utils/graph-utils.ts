import { GraphEdge } from '@types'
import { EDGE_UNIDIRECTED_ACTIVE, EDGE_UNIDIRECTED_INACTIVE } from 'components/graph-panel/graph/graph-config'
import Cytoscape from 'cytoscape'

let core: Cytoscape.Core

export const setCore = (newCore: Cytoscape.Core) => (core = newCore)

/**
 * Method constructs the set of edges based on agents connections
 *
 * @param {GraphEdge[]}[connections] - set of all connections
 *
 * @returns GraphEdge[]
 */
export const constructEdges = (connections: GraphEdge[]) =>
   connections.map((connection) => {
      core
         ?.edges()
         ?.$id(connection.data.id)
         .css({ ...getEdgeStyle(connection) })
      return connection
   })

const getEdgeStyle = (edge: GraphEdge) =>
   edge.state === 'active' ? EDGE_UNIDIRECTED_ACTIVE : EDGE_UNIDIRECTED_INACTIVE
