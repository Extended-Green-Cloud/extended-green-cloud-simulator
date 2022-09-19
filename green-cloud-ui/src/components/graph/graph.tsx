import React, { useEffect, useState } from 'react'
import CytoscapeComponent from "react-cytoscapejs";
import Cytoscape from "cytoscape";
import fcose from "cytoscape-fcose";
import { GRAPH_LAYOUT, GRAPH_STYLE, GRAPH_STYLESHEET } from './graph-config';

import { cloudNetworkActions, useAppDispatch, useAppSelector } from "@store";
import { createEdgesForAgent, createNodeForAgent } from '@utils';

import { MOCK_AGENTS } from 'views/main-view/main-view';

Cytoscape.use(fcose)

/**
 * Component representing the graph canvas implemented using cytoscape library
 * 
 * @returns Cytoscape graph 
 */
export const DisplayGraph = () => {
  // eslint-disable-next-line
  const [reactCy, setCy] = useState<Cytoscape.Core>()
  const { agents } = useAppSelector(state => state.cloudNetwork)
  const dispatch = useAppDispatch()

  //TODO: Remove this useEffect after we'll have the real data
  useEffect(() => {
    MOCK_AGENTS.forEach(agent => dispatch(cloudNetworkActions.registerAgent(agent)))
    // eslint-disable-next-line
  }, [])

  const elements = CytoscapeComponent.normalizeElements({
    nodes: agents.map(agent => { return ({ data: createNodeForAgent(agent) }) }),
    edges: agents.flatMap(agent => createEdgesForAgent(agent))
  })

  const cy = (core: Cytoscape.Core): void => {
    setCy(core)

    core.on('add', 'node', event => {
      core.layout(GRAPH_LAYOUT).run()
      core.fit()
    })

    core.on('tap', 'node', event => {
      dispatch(cloudNetworkActions.setSelectedAgent(event.target.id()))
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