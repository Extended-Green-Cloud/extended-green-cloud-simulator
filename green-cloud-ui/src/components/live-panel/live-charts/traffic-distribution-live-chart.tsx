import React from 'react'

import { LiveChartWrapper } from '@components'
import {
   AgentType,
   CloudNetworkAgent,
   CommonAgentReports,
   LiveChartDataCategory,
   MenuTab,
   ReportsStore,
   ServerAgent
} from '@types'
import LiveBarChart from 'components/live-panel/live-chart-components/live-chart-types/live-bar-chart'
import { useSelector } from 'react-redux'
import { RootState, selectChosenNetworkAgent, selectSelectedTab } from '@store'

interface Props {
   reports: ReportsStore
}

/**
 * Live chart that displays the distribution of traffic between selected agents
 *
 * @param {ReportsStore}[reports] - all store reports
 * @returns JSX Element
 */
export const TrafficDistributionLiveChart = ({ reports }: Props) => {
   const selectedAgent = useSelector((state: RootState) => selectChosenNetworkAgent(state))
   const selectedTab = useSelector((state: RootState) => selectSelectedTab(state))
   const connectedAgents =
      selectedTab !== MenuTab.CLOUD_SUMMARY && selectedAgent !== null
         ? selectedAgent?.type === AgentType.CLOUD_NETWORK
            ? { type: 'Servers', agents: (selectedAgent as CloudNetworkAgent).serverAgents }
            : { type: 'Green Sources', agents: (selectedAgent as ServerAgent).greenEnergyAgents }
         : { type: 'CNA', agents: [] }
   const agentReports =
      connectedAgents.agents.length > 0
         ? reports.agentsReports.filter((report) => connectedAgents.agents.includes(report.name))
         : reports.agentsReports.filter((report) => report.name.includes('CNA'))

   const getChartData = (): LiveChartDataCategory[] => {
      const cnaTraffics = agentReports
         .map((agentReports) => (agentReports.reports as CommonAgentReports).trafficReport)
         .map((trafficReport) => (trafficReport.length === 0 ? 0 : trafficReport[trafficReport.length - 1]?.value ?? 0))

      const overallTraffic = cnaTraffics.reduce((sum, val) => sum + val, 0)

      return agentReports.map((agent, idx) => ({
         name: `${agent.name} traffic`,
         color: `var(--green-${idx + 2})`,
         statistics: overallTraffic === 0 ? 0 : (cnaTraffics[idx] / overallTraffic) * 100
      }))
   }

   const formatLabel = (label: string) => [label, '%'].join('')

   return (
      <LiveChartWrapper
         {...{
            title: `${selectedAgent?.name ?? 'CNA'} traffic distribution per ${connectedAgents.type}`,
            chart: LiveBarChart,
            data: getChartData(),
            additionalProps: {
               valueDomain: [0, 100],
               yAxisFormatter: formatLabel
            },
            disableTimeSelector: true
         }}
      />
   )
}

export default TrafficDistributionLiveChart
