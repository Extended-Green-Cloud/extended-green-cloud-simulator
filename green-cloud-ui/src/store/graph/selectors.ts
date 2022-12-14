import { createSelector } from '@reduxjs/toolkit'
import { RootState } from 'store/store'

const graphSelector = (state: RootState) => state.graph

/**
 * Method returns all network nodes
 */
export const selectNetworkNodes = createSelector(
   [graphSelector],
   (graphSelector) => graphSelector.nodes
)

/**
 * Method returns existing network connections
 */
export const selectExistingConnections = createSelector(
   [graphSelector],
   (graphSelector) => {
      const nodes = graphSelector.nodes

      return graphSelector.connections.filter(
         (edge) =>
            nodes.some((node) => node.id === edge.data.target) &&
            nodes.some((node) => node.id === edge.data.source)
      )
   }
)
