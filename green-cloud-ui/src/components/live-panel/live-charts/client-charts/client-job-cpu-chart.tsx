import React from 'react'

import { LiveChartWrapper, LiveLineChart } from '@components'
import { LiveChartDataCategory, LiveChartEntry } from '@types'
import { getJobResourceVal } from 'utils/job-utils'

interface Props {
   minCpuReport: LiveChartEntry[]
   avgCpuReport: LiveChartEntry[]
   maxCpuReport: LiveChartEntry[]
}

/**
 * Live chart that displays the average, minimal and maximal job execution size over time
 *
 * @param {LiveChartEntry}[minCpuReport] - report of min job CPU requirements
 * @param {LiveChartEntry}[avgCpuReport] - report of avg job CPU requirements
 * @param {LiveChartEntry}[maxCpuReport] - report of max job CPU requirements
 * @returns JSX Element
 */
export const JobCPURequirementLiveChart = ({ minCpuReport, avgCpuReport, maxCpuReport }: Props) => {
   const chartData: LiveChartDataCategory[] = [
      { name: 'max. job CPU', color: 'var(--red-1)', statistics: maxCpuReport },
      { name: 'min. job CPU', color: 'var(--green-1)', statistics: minCpuReport },
      { name: 'avg. job CPU', color: 'var(--orange-1)', statistics: avgCpuReport }
   ]

   const formatLabel = (label: string) => getJobResourceVal(parseInt(label))

   return (
      <LiveChartWrapper
         {...{
            title: 'Job CPU averages over time',
            chart: LiveLineChart,
            data: chartData,
            additionalProps: { yAxisFormatter: formatLabel }
         }}
      />
   )
}

export default JobCPURequirementLiveChart
