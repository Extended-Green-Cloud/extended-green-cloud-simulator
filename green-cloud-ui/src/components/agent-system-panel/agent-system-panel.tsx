import React, { useState } from 'react'

import './cloud-statistics/cloud-config'
import { styles } from './agent-system-panel-styles'

import { Card } from '@components'
import CloudStatistics from './cloud-statistics/cloud-statistics'
import TabHeader from 'components/common/tab-header/tab-header'
import JobSchedule from './schedule-statistics/job-schedule'
import AgentStatisticsPanel from './agent-statistics/agent-statistics'
import ClientPanel from './client-statistics/client-statistics'
import { AgentStore } from '@types'
import { useAppSelector } from '@store'

const statisticsHeader = 'Cloud Panel'
const scheduleHeader = 'Job Schedule'
const agentsHeader = 'Agents Panel'
const clientsHeader = 'Clients Panel'

const tabTitles = [
   statisticsHeader,
   scheduleHeader,
   agentsHeader,
   clientsHeader,
]

/**
 * Component represents a panel gathering all infromations about cloud network
 *
 * @returns JSX Element
 */
const AgentSystemPanel = () => {
   const [selectedTabIdx, setSelectedTabIdx] = useState(0)
   const clientIdx = 3

   const agentState: AgentStore = useAppSelector((state) => state.agents)
   const selectedAgent = agentState.agents.find(
      (agent) => agent.name === agentState.selectedAgent
   )

   const tabs = [
      <CloudStatistics />,
      <JobSchedule />,
      <AgentStatisticsPanel {...{ selectedAgent }} />,
      <ClientPanel />,
   ]

   return (
      <Card
         {...{
            header: (
               <TabHeader
                  {...{
                     tabTitles,
                     selectedTabIdx,
                     setSelectedTabIdx,
                  }}
               />
            ),
            containerStyle: styles.cloudContainer,
            removeScroll: selectedTabIdx === clientIdx,
         }}
      >
         {tabs[selectedTabIdx]}
      </Card>
   )
}

export default AgentSystemPanel
