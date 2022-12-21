import React from 'react'

import { styles } from './adaptation-goals-styles'

import DetailsField from 'components/common/details-field/details-field'
import { AdaptationGoal } from '@types'

interface Props {
   goals: AdaptationGoal[]
}

/**
 * Component represents tab gathering data regarding current adaptation goals in the system
 *
 * @returns JSX Element
 */
export const AdaptationGoals = ({ goals }: Props) => {
   const {
      containerStyle,
      headerStyle,
      fieldContainerStyle,
      fieldLabelStyle,
      fieldValueStyle,
   } = styles
   const generateGoalFields = () => {
      return goals.map((goal) => {
         const { name, threshold, isAboveThreshold, weight } = goal
         const goalArrow = isAboveThreshold ? '\u2191' : '\u2193'
         const thresholdPercentage = [threshold * 100, '%'].join('')
         const goalVal = [goalArrow, thresholdPercentage].join(' ')
         const valuesMap = [
            { value: goalVal, label: 'Threshold' },
            { value: weight, label: 'Weight' },
         ]

         return (
            <div style={containerStyle}>
               <div style={headerStyle}>{name.toUpperCase()}</div>
               {valuesMap.map(({ value, label }) => (
                  <DetailsField
                     {...{
                        label,
                        value,
                        fieldContainerStyle,
                        fieldLabelStyle,
                        fieldValueStyle,
                     }}
                  />
               ))}
            </div>
         )
      })
   }

   return <div>{generateGoalFields()}</div>
}
