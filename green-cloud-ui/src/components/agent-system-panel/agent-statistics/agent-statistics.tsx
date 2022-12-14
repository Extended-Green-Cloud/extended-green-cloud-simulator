import React from 'react'
import './agent-statistics-config'
import DetailsField from '../../common/details-field/details-field'
import {
   getAgentFields,
   getStatisticsMapForAgent,
   MAP_TYPE,
   PERCENTAGE_VALUES,
} from './agent-statistics-config'
import SubtitleContainer from '../../common/subtitle-container/subtitle-container'

import { Agent, AgentType } from '@types'
import Badge from 'components/common/badge/badge'
import { styles } from './agent-statistics-styles'

interface Props {
   selectedAgent?: Agent
}

const description = 'Click on an agent to display its statistics'

/**
 * Component represents panel gathering all information about given agent
 *
 * @param {Agent}[selectedAgent] - agent for which the statistics are displayed
 * @returns JSX Element
 */
const AgentStatisticsPanel = ({ selectedAgent }: Props) => {
   const includeQualityMap = selectedAgent
      ? [
           AgentType.CLOUD_NETWORK,
           AgentType.SERVER,
           AgentType.GREEN_ENERGY,
        ].includes(selectedAgent.type)
      : false
   const { fieldHeader, fieldWrapper } = styles

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

      return PERCENTAGE_VALUES.includes(key) && value
         ? [(value as number).toFixed(2), '%'].join('')
         : value !== ''
         ? value
         : 0
   }

   const generateDetailsFields = (type?: string) => {
      if (selectedAgent) {
         const map = getStatisticsMapForAgent(selectedAgent, type)
         const header = type ? MAP_TYPE.QUALITY : MAP_TYPE.STATE
         return (
            <div>
               {!type && (
                  <DetailsField
                     {...{ label: selectedAgent.name, isHeader: true }}
                  />
               )}
               <div style={fieldWrapper}>
                  <div style={fieldHeader}>{header}</div>
                  {mapToStatistics(selectedAgent, map)}
               </div>
            </div>
         )
      } else {
         return <SubtitleContainer text={description} />
      }
   }

   return (
      <div>
         {generateDetailsFields()}
         {includeQualityMap && generateDetailsFields(MAP_TYPE.QUALITY)}
      </div>
   )
}

export default AgentStatisticsPanel
