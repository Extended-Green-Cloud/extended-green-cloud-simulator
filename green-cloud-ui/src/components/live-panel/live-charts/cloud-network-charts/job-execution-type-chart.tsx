import React from 'react'

import { LiveChartWrapper } from '@components'
import { LiveChartDataCategory } from '@types'
import LivePieChart from 'components/live-panel/live-chart-components/live-chart-types/live-pie-chart'

interface Props {
   jobsExecutedInCloud: number
   jobsExecutedWithGreen: number
}

/**
 * Live chart that displays pie chart with percentage on how many jobs has been executed in cloud to jobs executed with green energy
 *
 * @param {string}[jobsExecutedInCloud] - number of jobs executed in central cloud
 * @param {string}[jobsExecutedWithGreen] - number of jobs executed with green energy
 * @returns JSX Element
 */
export const JobExecutionTypeChart = ({ jobsExecutedInCloud, jobsExecutedWithGreen }: Props) => {
   const chartData: LiveChartDataCategory[] = [
      { name: 'jobs executed in central cloud no.', color: 'var(--red-1)', statistics: jobsExecutedInCloud },
      { name: 'jobs executed by servers no.', color: 'var(--green-1)', statistics: jobsExecutedWithGreen }
   ]

   const formatLabel = (label: string) => [label, '%'].join('')

   return (
      <LiveChartWrapper
         {...{
            title: 'Number of jobs executed in central cloud to executed by servers',
            chart: LivePieChart,
            data: chartData,
            additionalProps: {
               valueDomain: [0, 100],
               yAxisFormatter: formatLabel
            },
            disableTimeSelector: true
         }}
      />
   )
}

export default JobExecutionTypeChart
