import React from 'react'

import { LiveChartWrapper } from '@components'
import LivePieChart from 'components/live-panel/live-chart-components/live-chart-types/live-pie-chart'
import { PIE_COLORS } from 'components/live-panel/config/live-panel-config'
import { useSelector } from 'react-redux'
import { RootState, selectClients } from '@store'

/**
 * Live chart that displays the division between process types that were executed
 *
 * @param {ClientAgentStatus[]}[clients] - adaptation goals taken into account in the system
 * @returns JSX Element
 */
export const ClientTypePieChart = () => {
   const clients = useSelector((state: RootState) => selectClients(state))
   const clientAggregations = clients.reduce((count, client) => {
      const processor = client.processorName
      if (!Object.prototype.hasOwnProperty.call(count, processor)) {
         count[processor] = 0
      } else {
         count[processor] += 1
      }
      return count
   }, {} as { [key: string]: number })

   const getClientPercentage = (val: number) => (clients.length > 0 ? val / clients.length : 0)

   const generatePieData = () =>
      Object.entries(clientAggregations).map((entry, idx) => ({
         name: entry[0],
         color: PIE_COLORS[idx % PIE_COLORS.length],
         statistics: getClientPercentage(entry[1]) * 100
      }))

   const formatLabel = (label: string) => [label, '%'].join('')

   return (
      <LiveChartWrapper
         {...{
            title: 'Division between different processor types',
            chart: LivePieChart,
            data: generatePieData(),
            additionalProps: {
               valueDomain: [0, 100],
               yAxisFormatter: formatLabel
            },
            disableTimeSelector: true
         }}
      />
   )
}

export default ClientTypePieChart
