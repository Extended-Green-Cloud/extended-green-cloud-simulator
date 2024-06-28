import React from 'react'

import { LiveChartWrapper, LiveLineChart } from '@components'
import { LiveChartDataCategory, LiveChartEntry } from '@types'

interface Props {
   allocationSuccessRatioReport: LiveChartEntry[]
   allocationAcceptanceRatioReport: LiveChartEntry[]
}

/**
 * Live chart that displays the information about average allocation success ratio and average acceptance ratio
 *
 * @param {LiveChartEntry[]}[allocationSuccessRatioReport] - report of allocation success ratio
 * @param {LiveChartEntry[]}[allocationAcceptanceRatioReport] - report of allocation acceptance ratio
 * @returns JSX Element
 */
export const AllocationRatioChart = ({ allocationSuccessRatioReport, allocationAcceptanceRatioReport }: Props) => {
   const chartData: LiveChartDataCategory[] = [
      { name: 'asr %', color: 'var(--green-1)', statistics: allocationSuccessRatioReport },
      { name: 'aar %', color: 'var(--green-4)', statistics: allocationAcceptanceRatioReport }
   ]

   const formatLabel = (label: string) => [label, '%'].join('')

   return (
      <LiveChartWrapper
         {...{
            title: '% of allocation acceptance and success',
            chart: LiveLineChart,
            data: chartData,
            additionalProps: {
               valueDomain: [0, 100],
               yAxisFormatter: formatLabel
            }
         }}
      />
   )
}

export default AllocationRatioChart
