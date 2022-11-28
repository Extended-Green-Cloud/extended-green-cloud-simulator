import React, { useState } from 'react'

import { SCHEDULER_CONFIGURATION } from './job-schedule-config'

import { useAppSelector } from '@store'
import DetailsField from 'components/common/details-field/details-field'
import ScheduleModal from '../../agent-system-panel/schedule-statistics/schedule-modal/schedule-modal'
import { ModalButton } from 'components/common'

const modalButtonText = 'Schedule'

/**
 * Component represents a panel gathering all statistics regarding job scheduling
 *
 * @returns JSX Element
 */
const JobSchedule = () => {
   const [isOpen, setIsOpen] = useState(false)
   const scheduler = useAppSelector((state) => state.cloudNetwork.scheduler)

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

export default JobSchedule
