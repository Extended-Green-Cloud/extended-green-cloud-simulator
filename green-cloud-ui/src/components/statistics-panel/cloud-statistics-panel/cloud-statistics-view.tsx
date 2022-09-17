import React from "react"
import { Card } from '@components'
import './cloud-statistics-config'
import { styles } from "./cloud-statistics-view-styles"
import { DetailField } from "@types"
import { useAppSelector } from "@store"
import { CURRENT_CLOUD_STATISTICS } from "./cloud-statistics-config"
import { DetailsField } from "../details-field/details-field"

const header = 'Cloud Network Statistics'

const CloudStatisticsCard = () => {
    const cloudNetworkState = useAppSelector(state => state.cloudNetwork)

    const generateDetailsFields = (statisticsMap: DetailField[]) => {
        return (
            <div>
                {statisticsMap.map(field => {
                    const { agents, ...cloudNetworkData} = cloudNetworkState
                    const value = {...cloudNetworkData}[field.key] ?? ''
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

export default CloudStatisticsCard