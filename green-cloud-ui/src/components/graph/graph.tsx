import React, { useContext, useEffect } from 'react'
import CytoscapeComponent from "react-cytoscapejs";
import Cytoscape from "cytoscape";
import fcose from "cytoscape-fcose";
import { GRAPH_LAYOUT, GRAPH_STYLE, GRAPH_STYLESHEET } from './graph-config';

import { agentsActions, useAppDispatch, useAppSelector } from "@store";
import { createNodeForAgent, selectExistingEdges, setCore } from '@utils';

import { AgentType } from '@types';
import { MOCK_AGENTS } from 'views/main-view/main-view';

Cytoscape.use(fcose)

/**
 * Component representing the graph canvas implemented using cytoscape library
 * 
 * @returns Cytoscape graph 
 */
export const DisplayGraph = () => {
  //const { setSelectedNetworkAgent } = useContext(MainAgentContext)
  const agentsState = useAppSelector(state => state.agents)
  const dispatch = useAppDispatch()
  const graphNodes = agentsState.agents.filter(agent => agent.type !== AgentType.CLIENT)

  //TODO: Remove this useEffect after we'll have the real data
  useEffect(() => {
    MOCK_AGENTS.forEach(agent => dispatch(agentsActions.registerAgent(agent)))
    // eslint-disable-next-line
  }, [])

  const elements = CytoscapeComponent.normalizeElements({
    nodes: graphNodes.map(agent => { return ({ data: createNodeForAgent(agent) }) }),
    edges: selectExistingEdges(agentsState.agents, agentsState.connections)
  })

  const cy = (core: Cytoscape.Core): void => {
    setCore(core)

    core.on('add', 'node', event => {
      core.layout(GRAPH_LAYOUT).run()
      core.fit()
    })

    core.on('tap', 'node', event => {
      dispatch(agentsActions.setSelectedAgent(event.target.id()))
    })
  }

  return (
    <CytoscapeComponent
      layout={GRAPH_LAYOUT}
      style={GRAPH_STYLE}
      stylesheet={GRAPH_STYLESHEET}
      minZoom={0.5}
      maxZoom={1}
      wheelSensitivity={0.1}
      {...{ cy, elements }}
    />
  )
}