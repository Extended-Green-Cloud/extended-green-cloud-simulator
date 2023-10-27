import React from 'react'
import './agent-statistics-config'
import DetailsField from '../../../common/details-field/details-field'
import {
   BADGE_STATE_COLORS,
   getAgentFields,
   getStatisticsMapForAgent,
   MAPS_FOR_AGENT_TYPE,
   PERCENTAGE_VALUES,
   StateTypes
} from './agent-statistics-config'
import SubtitleContainer from '../../../common/subtitle-container/subtitle-container'

import { Agent, AgentType, MultiLevelDetails } from '@types'
import Badge from 'components/common/badge/badge'
import { styles } from './agent-statistics-styles'
import { Header } from 'components/common'
import MultiLevelDetailsField from 'components/common/multi-level-detils-field/multi-level-details-field'

interface Props {
   selectedAgent?: Agent | null
}

const description = 'Click on an agent to display its statistics'

/**
 * Component represents panel gathering all information about given agent
 *
 * @param {Agent}[selectedAgent] - agent for which the statistics are displayed
 * @returns JSX Element
 */
const AgentStatisticsPanel = ({ selectedAgent }: Props) => {
   const mapsForAgent = MAPS_FOR_AGENT_TYPE[selectedAgent?.type ?? AgentType.MONITORING]
   const { fieldWrapper } = styles

   const mapToStatistics = (agent: Agent, statisticsMap: any[]) => {
      return statisticsMap.map((field) => {
         const { label, key } = field
         const agentFields = getAgentFields(agent)
         const agentValue = { ...(agentFields as any) }[key] ?? ''

         if (key === 'resourceMap') {
            return <MultiLevelDetailsField {...{ detailsFieldMap: agentValue as MultiLevelDetails[] }} />
         }

         const value = formatAgentValue(agentValue, key)
         const property = key === 'isActive' ? 'valueObject' : 'value'

         return <DetailsField key={key} {...{ label, [property]: value }} />
      })
   }

   const formatAgentValue = (value: string | number, key: string) => {
      if (key === 'isActive') return <Badge text={value as string} color={BADGE_STATE_COLORS[value as StateTypes]} />

      return PERCENTAGE_VALUES.includes(key) && value
         ? [(value as number).toFixed(2), '%'].join('')
         : value !== ''
         ? value
         : 0
   }

   const generateDetailsFields = (type: string) => {
      if (selectedAgent) {
         const map = getStatisticsMapForAgent(selectedAgent, type)
         return (
            <div>
               <div style={fieldWrapper}>
                  <Header {...{ text: type }} />
                  {mapToStatistics(selectedAgent, map)}
               </div>
            </div>
         )
      } else {
         return <SubtitleContainer text={description} />
      }
   }

   return <div>{mapsForAgent?.map((mapType) => generateDetailsFields(mapType))}</div>
}

export default AgentStatisticsPanel
