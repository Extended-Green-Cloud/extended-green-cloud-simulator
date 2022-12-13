import { ClientAgent } from '@types'
import { DetailsField } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { styles } from './client-job-duration-modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
   client: ClientAgent
}

/**
 * Component represents a pop-up modal displaying current duration map for client job statuses
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @param {ClientAgent}[client] - selected client
 * @returns JSX Element
 */
const ClientJobDurationModal = ({ isOpen, setIsOpen, client }: Props) => {
   const { modalStyle, valueStyle } = styles

   const header = 'JOB EXECUTION DURATION PER STATUS'

   const getStatusesDuration = () => {
      return Object.entries(client.durationMap).map(([key, value]) => {
         const trueValue = client.isSplit
            ? value / client.splitJobs.length
            : value

         return (
            <DetailsField
               {...{
                  key,
                  label: key,
                  valueObject: getDuration(key, trueValue),
                  fieldValueStyle: valueStyle,
               }}
            />
         )
      })
   }

   const getDuration = (key: string, val: number) => {
      if (['PROCESSED', 'CREATED', 'SCHEDULED'].includes(key)) {
         const minutes = Math.floor(val / 60000)
         const seconds = parseInt(((val % 60000) / 1000).toFixed(0))

         return minutes > 0
            ? `${minutes} MINUTES ${seconds} SECONDS`
            : `${seconds} SECONDS`
      }
      const hours = Math.floor(val / 60)
      const minutes = parseInt((val % 60).toFixed(0))
      const minReminderFixed = minutes === 60 ? 0 : minutes

      return hours > 0
         ? `${hours} HOURS ${minReminderFixed} MINUTES`
         : `${minReminderFixed} MINUTES`
   }

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            contentStyle: modalStyle,
            header: header,
         }}
      >
         {getStatusesDuration()}
      </Modal>
   )
}

export default ClientJobDurationModal
