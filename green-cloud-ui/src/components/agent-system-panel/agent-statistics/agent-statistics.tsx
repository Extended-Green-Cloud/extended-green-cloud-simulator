import React from 'react'
import './agent-statistics-config'
import DetailsField from '../../common/details-field/details-field'
import {
   getAgentFields,
   getStatisticsMapForAgent,
} from './agent-statistics-config'
import SubtitleContainer from '../../common/subtitle-container/subtitle-container'

import { Agent } from '@types'
import Badge from 'components/common/badge/badge'

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
         return (
            <div>
               <DetailsField
                  {...{ label: selectedAgent.name, isHeader: true }}
               />
               {mapToStatistics(selectedAgent, map)}
            </div>
         )
      } else {
         return <SubtitleContainer text={description} />
      }
   }

   return <div>{generateDetailsFields()}</div>
}

export default AgentStatisticsPanel
