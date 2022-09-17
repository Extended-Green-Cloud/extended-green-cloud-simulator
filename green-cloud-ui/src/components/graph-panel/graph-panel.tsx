import Card from 'components/card/card'
import { DisplayGraph } from 'components/graph/graph'
import React from 'react'
import { styles } from './graph-panel-styles'


const GraphPanel = () => {
    return (
        <Card header="Cloud network structure" containerStyle={styles.graphContainer}>
            <DisplayGraph />
        </Card>
    )
}

export default GraphPanel