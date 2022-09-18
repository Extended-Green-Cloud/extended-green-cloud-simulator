import React from "react"
import './agent-statistics-config'
import { DetailsField } from "../details-field/details-field"
import { getStatisticsMapForAgent } from "./agent-statistics-config"
import { styles } from './agent-statistics-panel-styles'
import { Agent, AgentType, CloudNetworkAgent, GreenEnergyAgent, MonitoringAgent, ServerAgent } from "@types"
import { useAppSelector } from "@store"
import { Card } from '@components'

const CloudStatisticsPanel = () => {
    const cloudNetworkState = useAppSelector(state => state.cloudNetwork)

    const header = 'Agent Statistics'.toUpperCase()

    const getHeader = () => {
        return !cloudNetworkState.selectedAgent ?
            header :
            <div style={styles.agentHeader}>
                {header}:<span style={styles.agentNameHeader}>
                    {cloudNetworkState.selectedAgent.name.toUpperCase()}
                </span>
            </div>
    }

    const getActiveBadge = (state: string) => {
        const badgeStyle = state === 'ACTIVE' ? styles.activeBadge : styles.inActiveBadge
        return (<span style={{ ...styles.badge, ...badgeStyle }}>{state}</span>)
    }

    const getAgentFields = (agent: Agent) => {
        if (agent.type === AgentType.SERVER) {
            const server = agent as ServerAgent
            const { isActive, ...data } = server
            const activeLabel = isActive ? 'ACTIVE' : 'INACTIVE'
            return ({ isActive: activeLabel, ...data })
        } else if (agent.type === AgentType.GREEN_ENERGY) {
            const greenEnergy = agent as GreenEnergyAgent
            const { isActive, agentLocation, ...data } = greenEnergy
            const { latitude, longitude } = agentLocation
            const activeLabel = isActive ? 'ACTIVE' : 'INACTIVE'
            return ({ isActive: activeLabel, latitude, longitude, ...data })
        } else if (agent.type === AgentType.CLOUD_NETWORK) {
            const cna = agent as CloudNetworkAgent
            const connectedServers = cna.serverAgents.length
            return ({ connectedServers, ...cna })
        } else if (agent.type === AgentType.MONITORING) {
            return ({ ...(agent as MonitoringAgent) })
        }
    }

    const mapToStatistics = (agent: Agent, statisticsMap: any[]) => {
        return statisticsMap.map(field => {
            const agentFields = getAgentFields(agent)
            const value = { ...agentFields as any }[field.key] ?? ''
            const valueObject = field.key === 'isActive' ? getActiveBadge(value) : undefined
            return (<DetailsField {...{ label: field.label, value, valueObject }} />)
        })
    }


    const generateDetailsFields = () => {
        if (cloudNetworkState.selectedAgent) {
            const agent = cloudNetworkState.selectedAgent
            const statisticsMap = getStatisticsMapForAgent(agent)
            return (<div>{mapToStatistics(agent, statisticsMap)}</div>)
        }
    }

    return (
        <Card {...{ header: getHeader(), containerStyle: styles.agentContainer }}>
            {generateDetailsFields()}
        </Card>
    )
}

export default CloudStatisticsPanel