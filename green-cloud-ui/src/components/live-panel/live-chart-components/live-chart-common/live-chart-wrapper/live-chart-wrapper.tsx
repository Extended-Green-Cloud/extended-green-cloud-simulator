import {
   DropdownOption,
   LiveChartDataCategory,
   LiveChartDataCategoryDescription,
   LiveChartEntryNumeric,
   LiveChartAdditionalProps,
   LiveChartEntryTime
} from '@types'
import { Dropdown } from 'components/common'
import { TIME_OPTIONS } from 'components/live-panel/config/live-panel-config'
import React, { useState } from 'react'
import LiveChartGeneric from '../live-chart-generic/live-chart-generic'

import { styles } from './live-chart-wrapper-styles'

interface Props {
   chart: React.ElementType<{
      data: LiveChartEntryTime[] | LiveChartEntryNumeric[]
      labels: LiveChartDataCategoryDescription[]
      [key: string]: any
   }>
   title: string
   data: LiveChartDataCategory[]
   additionalProps?: LiveChartAdditionalProps
   disableTimeSelector?: boolean
}

/**
 * Component represents a full chart container
 *
 * @param {ReactNode}[chart] - chart that is to be displayed
 * @param {string}[title] - title of the chart
 * @param {LiveChartDataCategory[]}[data] - data displayed in the chart
 * @param {boolean}[disableTimeSelector] - optional flag indicating if the time selector is to be disabled
 * @returns JSX Element
 */
export const LiveChartWrapper = ({ chart, title, data, additionalProps, disableTimeSelector }: Props) => {
   const { chartWrapper, chartTitle, headerWrapper, selectStyle } = styles

   const [selectedTime, setSelectedTime] = useState<DropdownOption>(TIME_OPTIONS.DAY)
   const onChange = (val: any) => setSelectedTime(val ? val : TIME_OPTIONS.DAY)

   return (
      <div style={chartWrapper}>
         <div style={headerWrapper}>
            <div style={chartTitle}>{title.toUpperCase()}</div>
            {!disableTimeSelector && (
               <Dropdown
                  {...{
                     value: selectedTime,
                     options: Object.keys(TIME_OPTIONS).map((key) => TIME_OPTIONS[key]),
                     onChange,
                     isMulti: false,
                     isClearable: false,
                     selectStyle
                  }}
               />
            )}
         </div>
         <LiveChartGeneric {...{ chart, data, timeRestriction: selectedTime.value as number, additionalProps }} />
         <></>
      </div>
   )
}

export default LiveChartWrapper
