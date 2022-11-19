import React, { useState } from 'react'
import { styles } from './statistics-panel-styles'

import { AgentStore } from '@types'
import { Card } from '@components'
import { useAppSelector } from '@store'
import AgentStatisticsPanel from './agent-statistics/agent-statistics'
import ClientPanel from './client-statistics/client-statistics'
import DoubleTabHeader from 'components/common/double-tab-header/double-tab-header'

const agentHeader = 'Agent Panel'
const clientHeader = 'Client Panel'

/**
 * Component represents panel rendering informations about Green Cloud Agents
 *
 * @returns JSX Element
 */
const StatisticsPanel = () => {
   const [isAgentPanel, setIsAgentPanel] = useState(true)
   const { agentContainer } = styles

   const agentState: AgentStore = useAppSelector((state) => state.agents)
   const selectedAgent = agentState.agents.find(
      (agent) => agent.name === agentState.selectedAgent
   )

   const getContent = () =>
      isAgentPanel ? (
         <AgentStatisticsPanel {...{ selectedAgent }} />
      ) : (
         <ClientPanel />
      )

   return (
      <Card
         {...{
            header: (
               <DoubleTabHeader
                  {...{
                     firstTabTitle: agentHeader,
                     secondTabTitle: clientHeader,
                     isFirstTabSelected: isAgentPanel,
                     setIsFirstTabSelected: setIsAgentPanel,
                  }}
               />
            ),
            containerStyle: agentContainer,
            removeScroll: !isAgentPanel,
         }}
      >
         {getContent()}
      </Card>
   )
}

export default StatisticsPanel
