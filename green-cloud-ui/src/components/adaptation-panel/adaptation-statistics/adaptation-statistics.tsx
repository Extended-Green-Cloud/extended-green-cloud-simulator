import React, { useState } from 'react'

import './css/system-indicator.css'

import { DetailsField } from '@components'
import {
   ADAPTATION_STATISTICS_FIELDS,
   COUNTERS,
} from '../adaptation-panel-config'
import { styles } from './adaptation-statistics-style'
import SystemIndicatorModal from './system-indicator-modal/system-indicator-modal'
import { AdaptationGoal, ManagingSystemStore } from '@types'
import { iconCheckMark, iconCross } from '@assets'
import InfoTooltip from 'components/common/info-tooltip/info-tooltip'

interface Props {
   managingSystem: ManagingSystemStore
   adaptationGoals: AdaptationGoal[]
}

const tooltipPercentHeader = "Current value of the goal's quality"
const tooltipIconHeader = "Information about the goal's fulfillment"

/**
 * Component represents fields containing adaptation statistics
 *
 * @returns JSX Element
 */
export const AdaptationStatistics = ({
   managingSystem,
   adaptationGoals,
}: Props) => {
   const [isOpen, setIsOpen] = useState(false)
   const {
      fieldContainerStyle,
      fieldValueStyle,
      fieldLabelStyle,
      iconStyle,
      goalWrapper,
      qualityText,
      tooltipContent,
   } = styles

   const tooltipIconContent = (
      <div style={tooltipContent}>
         <div>{'\u2713'} - goal is fulfilled</div>
         <div>
            <span style={{ fontSize: '0.55rem' }}>{'\u2573'}</span> - goal is
            not fulfilled
         </div>
      </div>
   )

   const getIndicatorLabel = (label: string) => (
      <div
         className="system-indicator"
         onClick={() => setIsOpen((curr) => !curr)}
      >
         {label.toUpperCase()}
      </div>
   )

   const prepareStatisticsForDisplay = () => {
      const { goalQualityIndicators, ...otherProperties } = managingSystem
      const goalQualityMap = goalQualityIndicators.map((entry) => ({
         ['goalId' + entry.id]: entry.quality,
      }))
      return {
         ...(Object.assign({}, ...(goalQualityMap as [])) as any),
         ...(otherProperties as any),
      }
   }

   const parseStatisticsValue = (key: string, value: any, label: string) => {
      if (COUNTERS.includes(key)) {
         return { parsedVal: value, parsedLabel: label, type: 'value' }
      }

      const percentageVal = [((value as number) * 100).toFixed(0), '%'].join('')

      if (key === 'systemIndicator') {
         return {
            parsedVal: percentageVal,
            parsedLabel: getIndicatorLabel(label),
            type: 'value',
         }
      }

      return {
         parsedVal: parseGoalQuality(percentageVal, key, value),
         parsedLabel: label,
         type: 'valueObject',
      }
   }

   const parseGoalQuality = (
      percentageVal: string,
      key: string,
      value: any
   ) => {
      const qualityGoal = parseInt(key.split('Id')[1])
      const goal = adaptationGoals.filter((goal) => goal.id === qualityGoal)[0]

      if (goal) {
         const isGoalFulfilled = goal.isAboveThreshold
            ? goal.threshold <= value
            : goal.threshold >= value

         const icon = isGoalFulfilled ? iconCheckMark : iconCross
         const color = isGoalFulfilled ? 'var(--green-1)' : 'var(--red-1)'
         const textStyle = { ...qualityText, ...{ color } }

         const tooltipIconId = 'tooltip-goal-icon-' + goal.id
         const tooltipQualityId = 'tooltip-goal-quality-' + goal.id

         return (
            <div style={goalWrapper}>
               <img
                  style={iconStyle}
                  src={icon}
                  id={tooltipIconId}
                  alt="Goal icon"
               />
               <span style={textStyle} id={tooltipQualityId}>
                  {['(', percentageVal, ')'].join('')}
               </span>
               <InfoTooltip
                  id={tooltipIconId}
                  content={tooltipIconContent}
                  header={tooltipIconHeader}
               />
               <InfoTooltip
                  id={tooltipQualityId}
                  header={tooltipPercentHeader}
               />
            </div>
         )
      } else return null
   }

   const generateDetailsFields = () => {
      const statistics = prepareStatisticsForDisplay()

      return ADAPTATION_STATISTICS_FIELDS.map((field) => {
         const { key, label } = field
         const value = statistics[key] ?? 0
         const { parsedVal, parsedLabel, type } = parseStatisticsValue(
            key,
            value,
            label
         )

         if (parsedVal !== null) {
            return (
               <DetailsField
                  {...{
                     key,
                     label: parsedLabel,
                     [type]: parsedVal,
                     fieldContainerStyle,
                     fieldValueStyle,
                     fieldLabelStyle,
                  }}
               />
            )
         }
      })
   }

   return (
      <div>
         {generateDetailsFields()}
         <SystemIndicatorModal {...{ isOpen, setIsOpen }} />
      </div>
   )
}
