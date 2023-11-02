import React from 'react'

import { LiveChartWrapper } from '@components'
import { LiveChartDataCategory } from '@types'
import LivePieChart from 'components/live-panel/live-chart-components/live-chart-types/live-pie-chart'
import { RootState, selectNetworkStatistics } from '@store'
import { useSelector } from 'react-redux'

/**
 * Live chart that displays pie chart with percentage on how many jobs has been executed in cloud to jobs executed with green energy
 *
 * @returns JSX Element
 */
export const JobExecutionTypeChart = () => {
   const { finishedJobsInCloudNo, finishedJobsNo } = useSelector((state: RootState) => selectNetworkStatistics(state))
   const jobsExecutedInCloud = (finishedJobsNo !== 0 ? finishedJobsInCloudNo / finishedJobsNo : 0) * 100
   const jobsExecutedWithGreen = finishedJobsNo !== 0 ? 100 - jobsExecutedInCloud : 0

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
