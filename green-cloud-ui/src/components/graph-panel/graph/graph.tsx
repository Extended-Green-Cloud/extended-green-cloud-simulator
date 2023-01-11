import React, { useMemo, useEffect, useRef, useState } from 'react'
import CytoscapeComponent from 'react-cytoscapejs'
import Cytoscape from 'cytoscape'
import fcose from 'cytoscape-fcose'
import { GRAPH_LAYOUT, GRAPH_STYLE, GRAPH_STYLESHEET } from './graph-config'

import { constructEdges, setCore } from '@utils'

import { AgentNode, GraphEdge, SchedulerAgent } from '@types'

Cytoscape.use(fcose)

interface Props {
   nodes: AgentNode[]
   connections: GraphEdge[]
   scheduler: SchedulerAgent | null
   setSelectedAgent: (id: string) => void
}

/**
 * Component representing the graph canvas implemented using cytoscape library
 *
 * @returns Cytoscape graph
 */
export const DisplayGraph = ({ nodes, connections, scheduler, setSelectedAgent }: Props) => {
   const [core, setCyCore] = useState<Cytoscape.Core>()
   const nodesRef = useRef<number>(-1)
   const elements = useMemo(
      () =>
         CytoscapeComponent.normalizeElements({
            nodes: nodes.map((node) => ({ data: { ...node } })),
            edges: constructEdges(connections),
         }),
      [nodes, connections]
   )

   useEffect(() => {
      if (nodesRef.current !== nodes.length) {
         if (core) {
            core.layout(GRAPH_LAYOUT).run()
            core.fit()
         }
         nodesRef.current = nodes.length
      }
   }, [nodes])

   const cy = (core: Cytoscape.Core): void => {
      setCore(core)
      setCyCore(core)

      core.on('tap', 'node', (event) => {
         if (event.target.id() !== scheduler?.name) {
            setSelectedAgent(event.target.id())
         }
      })
   }

   return (
      <CytoscapeComponent
         layout={GRAPH_LAYOUT}
         style={GRAPH_STYLE}
         stylesheet={GRAPH_STYLESHEET}
         minZoom={0.3}
         maxZoom={1}
         wheelSensitivity={0.1}
         {...{ cy, elements }}
      />
   )
}
