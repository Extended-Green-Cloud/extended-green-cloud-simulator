import React from "react"
import './cloud-statistics-config'
import { styles } from "./cloud-statistics-panel-styles"
import { CURRENT_CLOUD_STATISTICS } from "./cloud-statistics-config"
import { DetailsField } from "../details-field/details-field"
import { DetailField } from "@types"
import { useAppSelector } from "@store"
import { Card } from '@components'

const header = 'Cloud Network Statistics'

const CloudStatisticsPanel = () => {
    const cloudNetworkState = useAppSelector(state => state.cloudNetwork)

    const generateDetailsFields = (statisticsMap: DetailField[]) => {
        return (
            <div>
                {statisticsMap.map(field => {
                    const value = {...cloudNetworkState}[field.key] ?? ''
                    return (
                        <DetailsField {...{label: field.label, value}} />
                    )
                })}
            </div>
        )
    }

    return (
        <Card {...{ header, containerStyle: styles.cloudContainer }}>
            {generateDetailsFields(CURRENT_CLOUD_STATISTICS)}
        </Card>
    )
}

export default CloudStatisticsPanel