import React, { useState } from 'react'

import { styles } from './live-panel-styles'

import { Button, Card, SubtitleContainer } from '@components'
import { AdaptationAction, AdaptationGoal, Agent, AgentStatisticReport, MenuTab, ReportsStore } from '@types'
import { CHART_MODALS } from './config/live-panel-config'
import LiveChartModal from './live-chart-modal/live-chart-modal'
import LiveAdaptationModal from './live-adaptation-modal/live-adaptation-modal'

interface Props {
   selectedTab: MenuTab
   reports: ReportsStore
   agentReports: AgentStatisticReport | null
   adaptations: AdaptationAction[]
   systemGoals: AdaptationGoal[]
   agentData: Agent | null
   selectedAgent: string | null
}

const headerTitle = 'Live statistics'
const buttonTitle = 'Display all statistics'

/**
 * Component represents a panel gathering all information about cloud network
 *
 * @returns JSX Element
 */
export const LivePanel = ({
   selectedTab,
   reports,
   agentReports,
   adaptations,
   agentData,
   selectedAgent,
   systemGoals
}: Props) => {
   const [isOpen, setIsOpen] = useState<boolean>(false)
   const { mainContainer, contentContainer, headerContainer, headerStyle } = styles

   const isAgentTab = selectedTab === MenuTab.AGENTS
   const hasAgentData = agentData !== null && selectedAgent === agentData.name

   const isManagingTab = selectedTab === MenuTab.ADAPTATION
   const hasAdaptationData = adaptations.length > 0 && systemGoals.length > 0

   const hasData = (!isAgentTab && reports.clientsReport.length !== 0) || (isAgentTab && agentReports && hasAgentData)
   const reportsId = isAgentTab && agentReports ? `agent${agentReports?.type}` : selectedTab.toString()
   const chartsData = CHART_MODALS[reportsId]
   const isChartDashboardEnabled =
      (hasData && chartsData && !chartsData.disableChartDashboard && (isManagingTab ? hasAdaptationData : true)) ??
      false

   const buttonClassName = [
      'medium-green-button',
      isChartDashboardEnabled ? 'medium-green-button-active' : 'medium-green-button-disabled'
   ].join(' ')

   const header = (
      <div style={headerContainer}>
         <div style={headerStyle}>{headerTitle.toUpperCase()}</div>
         <Button
            {...{
               title: buttonTitle.toUpperCase(),
               onClick: () => setIsOpen(!isOpen),
               isDisabled: !isChartDashboardEnabled,
               buttonClassName
            }}
         />
      </div>
   )

   const getLiveModal = () =>
      selectedTab === MenuTab.ADAPTATION ? (
         <LiveAdaptationModal {...{ isOpen, setIsOpen, reports, agentReports, adaptations, systemGoals }} />
      ) : (
         <LiveChartModal {...{ isOpen, setIsOpen, reports, agentReports, adaptations, selectedTabId: reportsId }} />
      )

   return (
      <Card {...{ containerStyle: mainContainer, contentStyle: contentContainer, header, removeScroll: true }}>
         {!hasData || chartsData === undefined ? (
            <SubtitleContainer text="No data available yet" />
         ) : (
            <>
               {getLiveModal()}
               {chartsData.charts[chartsData.mainChartId](reports, agentReports)}
            </>
         )}
      </Card>
   )
}

export default LivePanel
