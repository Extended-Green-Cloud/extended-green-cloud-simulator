import React, { useState } from 'react'

import './cloud-statistics/cloud-config'
import { styles } from './agent-system-panel-styles'

import { Card } from '@components'
import CloudStatistics from './cloud-statistics/cloud-statistics-connected'
import TabHeader from 'components/common/tab-header/tab-header'
import JobSchedule from './schedule-statistics/job-schedule-connected'
import AgentStatisticsPanel from './agent-statistics/agent-statistics'
import ClientPanel from './client-statistics/client-statistics-connected'
import { Agent } from '@types'

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

interface Props {
   selectedAgent?: Agent
}

/**
 * Component represents a panel gathering all infromations about cloud network
 *
 * @returns JSX Element
 */
export const AgentSystemPanel = ({ selectedAgent }: Props) => {
   const [selectedTabIdx, setSelectedTabIdx] = useState(0)
   const clientIdx = 3

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
