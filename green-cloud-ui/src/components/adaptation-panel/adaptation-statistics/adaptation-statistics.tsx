import React, { useState } from 'react'

import './css/system-indicator.css'

import { DetailsField } from '@components'
import { useAppSelector } from '@store'
import {
   ADAPTATION_STATISTICS_FIELDS,
   PERCENTAGE_KEYS,
} from '../adaptation-panel-config'
import { styles } from './adaptation-statistics-style'
import SystemIndicatorModal from './system-indicator-modal/system-indicator-modal'

/**
 * Component represents fields containing adaptation statistics
 *
 * @returns JSX Element
 */
const AdaptationStatistics = () => {
   const [isOpen, setIsOpen] = useState(false)
   const { fieldContainerStyle, fieldValueStyle, fieldLabelStyle } = styles
   const managingSystem = useAppSelector((state) => state.managingSystem)

   const getIndicatorLabel = (label: string) => (
      <div
         className="system-indicator"
         onClick={() => setIsOpen((curr) => !curr)}
      >
         {label.toUpperCase()}
      </div>
   )

   const generateDetailsFields = () => {
      const statistics = { ...(managingSystem as any) }

      return ADAPTATION_STATISTICS_FIELDS.map((field) => {
         const { key, label } = field
         const value = statistics[key]
         const parsedVal = PERCENTAGE_KEYS.includes(key)
            ? [(value as number) * 100, '%'].join('')
            : value
         const parsedLabel =
            key === 'systemIndicator' ? getIndicatorLabel(label) : label
         return (
            <DetailsField
               {...{
                  key,
                  label: parsedLabel,
                  value: parsedVal,
                  fieldContainerStyle,
                  fieldValueStyle,
                  fieldLabelStyle,
               }}
            />
         )
      })
   }

   return (
      <div>
         {generateDetailsFields()}
         <SystemIndicatorModal {...{ isOpen, setIsOpen }} />
      </div>
   )
}

export default AdaptationStatistics
