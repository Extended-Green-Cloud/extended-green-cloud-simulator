import React from 'react'

import { styles } from './agent-panel-styles'

import { Agent, AgentType, SchedulerAgent } from '@types'
import AgentStatisticsPanel from './agent-statistics/agent-statistics'
import { SchedulerStatistics } from './scheduler-statistics/scheduler-statistics'

interface Props {
   selectedAgent?: Agent | null
}

/**
 * Component represents a panel gathering all infromations about regional manager agents
 *
 * @param {Agent}[selectedAgent] - agent for which the statistics are displayed
 * @returns JSX Element
 */
export const AgentPanel = ({ selectedAgent }: Props) => {
   const { containerStyle } = styles

   const selectedPanel =
      selectedAgent && selectedAgent?.type === AgentType.SCHEDULER ? (
         <SchedulerStatistics {...{ scheduler: selectedAgent as SchedulerAgent }} />
      ) : (
         <AgentStatisticsPanel {...{ selectedAgent }} />
      )

   return <div style={containerStyle}>{selectedPanel}</div>
}

export default AgentPanel
