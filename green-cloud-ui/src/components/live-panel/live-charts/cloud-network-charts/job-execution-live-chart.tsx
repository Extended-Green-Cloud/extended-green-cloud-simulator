import React from 'react'

import { LiveAreaChart, LiveChartWrapper } from '@components'
import { LiveChartDataCategory, LiveChartTooltip, LiveChartEntry } from '@types'
import { ContentType } from 'recharts/types/component/Tooltip'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'
import { renderCustomTooltipContent } from 'components/live-panel/live-chart-components/live-chart-common/live-chart-generic/live-chart-generic-config'
import { getJobResourceVal } from 'utils/job-utils'

interface Props {
   executedJobsReport: LiveChartEntry[]
   avgCpuReport: LiveChartEntry[]
   minCpuReport: LiveChartEntry[]
   maxCpuReport: LiveChartEntry[]
}

/**
 * Live chart that displays the number of executed jobs at particular time
 *
 * @param {string}[executedJobsReport] - report of currently executed jobs
 * @param {string}[avgCpuReport] - report of average required job CPU
 * @param {string}[minCpuReport] - report of minimum required job CPU
 *  @param {string}[maxCpuReport] - report of maximum required job CPU
 * @returns JSX Element
 */
export const JobExecutionLiveChart = ({ executedJobsReport, avgCpuReport, minCpuReport, maxCpuReport }: Props) => {
   const chartData: LiveChartDataCategory[] = [
      { name: 'executed jobs no.', color: 'var(--green-1)', statistics: executedJobsReport }
   ]

   const CustomTooltip: ContentType<ValueType, NameType> = ({ active, payload, label }) => {
      if (active && payload && payload.length) {
         const time = (label as Date).getTime()
         const data: LiveChartTooltip[] = [
            {
               name: 'avg. job CPU',
               value: parseCpuValue(avgCpuReport.filter((data) => data.time === time)[0]?.value as number)
            },
            {
               name: 'min. job CPU',
               value: parseCpuValue(minCpuReport.filter((data) => data.time === time)[0]?.value as number)
            },
            {
               name: 'max. job CPU',
               value: parseCpuValue(maxCpuReport.filter((data) => data.time === time)[0]?.value as number)
            }
         ]
         return renderCustomTooltipContent(label, payload, data)
      }
   }

   const parseCpuValue = (cpu: number) => getJobResourceVal(cpu / 1000)

   return (
      <LiveChartWrapper
         {...{
            title: 'Number of executed jobs over time',
            chart: LiveAreaChart,
            data: chartData,
            additionalProps: { customTooltip: CustomTooltip }
         }}
      />
   )
}

export default JobExecutionLiveChart
