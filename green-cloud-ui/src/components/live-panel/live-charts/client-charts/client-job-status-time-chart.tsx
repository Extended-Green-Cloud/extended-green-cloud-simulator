import React from 'react'

import { LiveChartWrapper } from '@components'
import { JobStatus, JobStatusMap, JobStatusReport, LiveChartDataCategory, LiveChartTooltip } from '@types'
import { PIE_COLORS } from 'components/live-panel/config/live-panel-config'
import LiveBarChart from 'components/live-panel/live-chart-components/live-chart-types/live-bar-chart'
import { getJobStatusDuration, getJobStatusTimeInMin } from 'utils/job-utils'
import { ContentType } from 'recharts/types/component/Tooltip'
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent'
import { renderCustomTooltipContent } from 'components/live-panel/live-chart-components/live-chart-common/live-chart-generic/live-chart-generic-config'

interface Props {
   aggregatedValues: JobStatusMap
   reportLength: number
}

const STATUESES_OF_INTEREST = [
   JobStatus.CREATED,
   JobStatus.DELAYED,
   JobStatus.IN_PROGRESS,
   JobStatus.ON_BACK_UP,
   JobStatus.ON_HOLD,
   JobStatus.PROCESSED
].map((key) => Object.keys(JobStatus)[Object.values(JobStatus).indexOf(key)])

/**
 * Live chart that displays the average time in which the jobs has been in each of the possible statuses
 *
 * @param {JobStatusMap}[aggregatedValues] - map of job status time
 * @param {number}[reportLength] - number of reported entries
 * @returns JSX Element
 */
export const JobAverageStatusTimeChart = ({ aggregatedValues, reportLength }: Props) => {
   const jobStatusReport: JobStatusReport[] = Object.entries(aggregatedValues)
      .map((entry) => ({
         status: entry[0],
         value: reportLength === 0 ? 0 : getJobStatusTimeInMin(entry[0], entry[1] / reportLength)
      }))
      .filter((entry) => STATUESES_OF_INTEREST.includes(entry.status))

   const chartData: LiveChartDataCategory[] = jobStatusReport.map((report, idx) => ({
      name: report.status.replaceAll('_', ' ').toLowerCase(),
      color: PIE_COLORS[idx],
      statistics: report.value
   }))

   const formatLabel = (label: string) => [label, 'min'].join(' ')
   const CustomTooltip: ContentType<ValueType, NameType> = ({ active, payload, label }) => {
      if (active && payload && payload.length) {
         const status = (label as string).replaceAll(' ', '_').toUpperCase()
         const duration = reportLength === 0 ? 0 : getJobStatusDuration(status, aggregatedValues[status] / reportLength)
         const data: LiveChartTooltip[] = [{ name: 'duration: ', value: duration }]
         return renderCustomTooltipContent(label.toUpperCase(), payload, data)
      }
   }

   return (
      <LiveChartWrapper
         {...{
            title: 'Avg. time of each job status',
            chart: LiveBarChart,
            data: chartData,
            disableTimeSelector: true,
            additionalProps: { unit: ' min', yAxisFormatter: formatLabel, customTooltip: CustomTooltip }
         }}
      />
   )
}

export default JobAverageStatusTimeChart
