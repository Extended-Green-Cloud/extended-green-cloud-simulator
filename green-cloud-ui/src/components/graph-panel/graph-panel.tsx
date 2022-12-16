import React from 'react'
import Card from 'components/common/card/card'
import DisplayGraph from 'components/graph-panel/graph/graph-connected'
import { styles } from './graph-panel-styles'

const header = 'Cloud network structure'

/**
 * Component is the graph container
 *
 * @returns JSX Element
 */
const GraphPanel = () => {
   return (
      <Card
         {...{
            header,
            containerStyle: styles.graphContainer,
            contentStyle: styles.graphContent,
            removeScroll: true,
         }}
      >
         <DisplayGraph />
      </Card>
   )
}

export default GraphPanel
