import React, { useState } from 'react'

import { SCHEDULER_CONFIGURATION } from './job-schedule-config'

import DetailsField from 'components/common/details-field/details-field'
import { ModalButton } from 'components/common'
import ScheduleModal from './schedule-modal/schedule-modal-connected'
import { SchedulerAgent } from '@types'

const modalButtonText = 'Schedule'

interface Props {
   scheduler: SchedulerAgent | null
}

/**
 * Component represents a panel gathering all statistics regarding job scheduling
 *
 * @returns JSX Element
 */
export const JobSchedule = ({ scheduler }: Props) => {
   const [isOpen, setIsOpen] = useState(false)

   const parseValue = (val: any, key: string) =>
      key !== 'maxQueueSize' ? [(val as number) * 100, '%'].join('') : val

   const mapStatisticsToFields = () => {
      return SCHEDULER_CONFIGURATION.map((field) => {
         const { key, label } = field
         const value = { ...scheduler }[key] ?? 0

         return (
            <DetailsField {...{ label, value: parseValue(value, key), key }} />
         )
      })
   }

   const getModalButton = (
      <ModalButton
         {...{
            buttonClassName: 'small-green-button',
            setIsOpen,
            title: modalButtonText.toUpperCase(),
         }}
      />
   )

   return (
      <div>
         {scheduler && (
            <DetailsField
               {...{
                  label: scheduler?.name ?? '',
                  isHeader: true,
                  valueObject: getModalButton,
               }}
            />
         )}
         {mapStatisticsToFields()}
         <ScheduleModal {...{ isOpen, setIsOpen }} />
      </div>
   )
}
