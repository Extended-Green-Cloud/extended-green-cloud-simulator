import React, { useState } from 'react'
import CytoscapeComponent from "react-cytoscapejs";
import Cytoscape from "cytoscape";
import fcose from "cytoscape-fcose";

import { useAppSelector } from "@store";
import { AgentNodeInterface, GraphEdge } from "@types";

import { GRAPH_LAYOUT, GRAPH_STYLE, GRAPH_STYLESHEET } from './graph-config';
import { createEdgesForAgent, createNodeForAgent } from 'utils';

Cytoscape.use(fcose)

export const DisplayGraph = () => {
  const [reactCy, setCy] = useState<Cytoscape.Core>()
  const { agents } = useAppSelector(state => state.cloudNetwork)

  const createNodes = () => agents.map(agent => {
    return ({ data: createNodeForAgent(agent) })
  })

  const createEdges = (): GraphEdge[] =>
    agents.flatMap(agent => createEdgesForAgent(agent))

  const elements = CytoscapeComponent.normalizeElements({
    nodes: createNodes(),
    edges: createEdges()
  })

  const cy = (core: Cytoscape.Core): void => setCy(core)

  return (
    <CytoscapeComponent
      layout={GRAPH_LAYOUT}
      style={GRAPH_STYLE}
      stylesheet={GRAPH_STYLESHEET}
      {...{ cy, elements }}
      minZoom={0.5}
      maxZoom={1}
      wheelSensitivity={0.1}
    />
  )
}