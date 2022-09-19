import React from "react"

import './cloud-statistics-config'
import { styles } from "./cloud-statistics-panel-styles"
import { CURRENT_CLOUD_STATISTICS } from "./cloud-statistics-config"
import DetailsField from "../details-field/details-field"

import { DetailField } from "@types"
import { useAppSelector } from "@store"
import { Card } from '@components'

const header = 'Cloud Network Statistics'

/**
 * Component represents a panel gathering all infromations about cloud network
 * 
 * @returns JSX Element
 */
const CloudStatisticsPanel = () => {
    const cloudNetworkState = useAppSelector(state => state.cloudNetwork)

    const mapStatisticsToFields = (statisticsMap: DetailField[]) => {
        return statisticsMap.map(field => {
            const { key, label } = field
            const value = { ...cloudNetworkState }[key] ?? ''
            return (<DetailsField {...{ label, value }} />)
        })
    }

    const generateDetailsFields = (statisticsMap: DetailField[]) => {
        return (<div>{mapStatisticsToFields(statisticsMap)}</div>)
    }

    return (
        <Card {...{ header, containerStyle: styles.cloudContainer }}>
            {generateDetailsFields(CURRENT_CLOUD_STATISTICS)}
        </Card>
    )
}

export default CloudStatisticsPanel