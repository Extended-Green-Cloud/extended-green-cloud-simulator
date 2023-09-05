import React from 'react'

import { LiveChartWrapper } from '@components'
import { LiveChartDataCategory, LiveChartEntry } from '@types'
import LiveBarOverTimeChart from 'components/live-panel/live-chart-components/live-chart-types/live-bar-over-time-chart'

interface Props {
   cpuPriorityReport: LiveChartEntry[]
   deadlinePriorityReport: LiveChartEntry[]
   title: string
}

/**
 * Live chart that displays the priorities of the scheduler over time
 *
 * @param {LiveChartEntry}[cpuPriorityReport] - report of scheduler power priority
 * @param {LiveChartEntry}[deadlinePriorityReport] - report of scheduler deadline priority
 * @param {string}[title] - title of the report
 * @returns JSX Element
 */
export const SchedulerPrioritiesLiveChart = ({ cpuPriorityReport, deadlinePriorityReport, title }: Props) => {
   const chartData: LiveChartDataCategory[] = [
      { name: 'power priority', color: 'var(--green-1)', statistics: cpuPriorityReport },
      { name: 'deadline priority', color: 'var(--green-2)', statistics: deadlinePriorityReport }
   ]

   return <LiveChartWrapper {...{ title, chart: LiveBarOverTimeChart, data: chartData }} />
}

export default SchedulerPrioritiesLiveChart
