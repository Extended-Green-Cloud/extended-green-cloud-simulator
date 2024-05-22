import React, { useState } from 'react'

import { SCHEDULER_CONFIGURATION } from './scheduler-statistics-config'

import DetailsField from 'components/common/details-field/details-field'
import { Header, ModalButton } from 'components/common'
import ScheduleModal from './scheduler-modal/scheduler-modal-connected'
import { CentralManagerAgent } from '@types'
import { styles } from './scheduler-statistics-styles'

const modalButtonText = 'Schedule'

interface Props {
   centralManager: CentralManagerAgent | null
}

/**
 * Component represents a panel gathering all statistics regarding job scheduling
 *
 * @returns JSX Element
 */
export const SchedulerStatistics = ({ centralManager }: Props) => {
   const [isOpen, setIsOpen] = useState(false)

   const parseValue = (val: any, key: string) =>
      key !== 'maxQueueSize' ? [((val as number) * 100).toFixed(0), '%'].join('') : val

   const mapStatisticsToFields = () => {
      return SCHEDULER_CONFIGURATION.map((field) => {
         const { key, label } = field
         const value = { ...centralManager }[key] ?? 0
         return <DetailsField {...{ label, value: parseValue(value, key), key }} />
      })
   }

   const getModalButton = (
      <ModalButton
         {...{
            buttonClassName: 'small-green-button',
            setIsOpen,
            title: modalButtonText.toUpperCase()
         }}
      />
   )

   return (
      <div>
         <DetailsField {...{ label: 'Currently scheduled jobs', valueObject: getModalButton }} />
         <div style={styles.configContainer}>
            <Header {...{ text: 'CONFIGUARATION' }} />
            {mapStatisticsToFields()}
         </div>
         <ScheduleModal {...{ isOpen, setIsOpen }} />
      </div>
   )
}
