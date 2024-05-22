import React from 'react'

import { LiveAreaChart, LiveChartWrapper } from '@components'
import { LiveChartDataCategory, LiveChartEntry } from '@types'

interface Props {
   queueCapacityReport: LiveChartEntry[]
   title: string
}

/**
 * Live chart that displays the central manager's queue capacity over time
 *
 * @param {LiveChartEntry}[queueCapacityReport] - number of jobs present in the queue
 * @param {string}[title] - title of the report
 * @returns JSX Element
 */
export const QueueCapacityLiveChart = ({ queueCapacityReport, title }: Props) => {
   const chartData: LiveChartDataCategory[] = [
      { name: 'no. of jobs in the queue', color: 'var(--green-1)', statistics: queueCapacityReport }
   ]

   return <LiveChartWrapper {...{ title, chart: LiveAreaChart, data: chartData }} />
}

export default QueueCapacityLiveChart
