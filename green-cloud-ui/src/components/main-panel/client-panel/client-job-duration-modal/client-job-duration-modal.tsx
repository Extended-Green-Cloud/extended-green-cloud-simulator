import { ClientAgent } from '@types'
import { DetailsField } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { styles } from './client-job-duration-modal-styles'
import { getJobStatusDuration } from 'utils/job-utils'

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
      return Object.entries(client.durationMap).map(([key, value]) => (
         <DetailsField
            {...{
               key,
               label: key,
               valueObject: getJobStatusDuration(key, value),
               fieldValueStyle: valueStyle
            }}
         />
      ))
   }

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            contentStyle: modalStyle,
            header: header
         }}
      >
         {getStatusesDuration()}
      </Modal>
   )
}

export default ClientJobDurationModal
