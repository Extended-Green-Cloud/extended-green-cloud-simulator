import React from 'react'
import './agent-panel-config'
import DetailsField from '../details-field/details-field'
import {
   getStatisticsMapForAgent,
   mapCloudNetworkAgentFields,
   mapGreenEnergyAgentFields,
   mapMonitoringAgentFields,
   mapServerAgentFields,
} from './agent-panel-config'
import SubtitleContainer from '../subtitle-container/subtitle-container'
import { styles } from './agent-panel-styles'

import {
   Agent,
   AgentStore,
   AgentType,
   CloudNetworkAgent,
   GreenEnergyAgent,
   MonitoringAgent,
   ServerAgent,
} from '@types'
import { Card } from '@components'
import Badge from 'components/badge/badge'
import { useAppSelector } from '@store'

const header = 'Agent Statistics'
const description = 'Click on an agent to display its statistics'

/**
 * Component represents panel gathering all information about given agent
 *
 * @returns JSX Element
 */
const AgentStatisticsPanel = () => {
   const agentState: AgentStore = useAppSelector((state) => state.agents)
   const selectedAgent = agentState.agents.find(
      (agent) => agent.name === agentState.selectedAgent
   )

   const getHeader = () => {
      return !selectedAgent ? (
         header.toUpperCase()
      ) : (
         <div style={styles.agentHeader}>
            <span>{header.toUpperCase()}:</span>
            <span style={styles.agentNameHeader}>
               {selectedAgent.name.toUpperCase()}
            </span>
         </div>
      )
   }

   const getAgentFields = (agent: Agent) => {
      switch (agent.type) {
         case AgentType.SERVER:
            return mapServerAgentFields(agent as ServerAgent)
         case AgentType.CLOUD_NETWORK:
            return mapCloudNetworkAgentFields(agent as CloudNetworkAgent)
         case AgentType.GREEN_ENERGY:
            return mapGreenEnergyAgentFields(agent as GreenEnergyAgent)
         case AgentType.MONITORING:
            return mapMonitoringAgentFields(agent as MonitoringAgent)
      }
   }

   const mapToStatistics = (agent: Agent, statisticsMap: any[]) => {
      return statisticsMap.map((field) => {
         const { label, key } = field
         const agentFields = getAgentFields(agent)
         const agentValue = { ...(agentFields as any) }[key] ?? ''
         const value = formatAgentValue(agentValue, key)
         const property = key === 'isActive' ? 'valueObject' : 'value'

         return <DetailsField {...{ label, [property]: value }} />
      })
   }

   const formatAgentValue = (value: string | number, key: string) => {
      if (key === 'isActive')
         return <Badge text={value as string} isActive={value === 'ACTIVE'} />

      return key === 'traffic' || key === 'backUpTraffic'
         ? [(value as number).toFixed(2), '%'].join('')
         : value
   }

   const generateDetailsFields = () => {
      if (selectedAgent) {
         const map = getStatisticsMapForAgent(selectedAgent)
         return <div>{mapToStatistics(selectedAgent, map)}</div>
      } else {
         return <SubtitleContainer text={description} />
      }
   }

   return (
      <Card
         {...{
            header: getHeader(),
            containerStyle: styles.agentContainer,
         }}
      >
         {generateDetailsFields()}
      </Card>
   )
}

export default AgentStatisticsPanel
