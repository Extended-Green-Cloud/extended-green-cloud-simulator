import React from 'react'
import Card from 'components/card/card'
import { DisplayGraph } from 'components/graph/graph'
import { styles } from './graph-panel-styles'

const header = 'Cloud network structure'

const GraphPanel = () => {
    return (
        <Card {...{header, containerStyle: styles.graphContainer, removeScroll: true}} >
            <DisplayGraph />
        </Card>
    )
}

export default GraphPanel