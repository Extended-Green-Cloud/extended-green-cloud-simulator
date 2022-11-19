import { ClientAgent, SplitJob } from '@types'
import { DetailsCard } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { parseSplitJobId } from 'utils/string-utils'
import { convertUnixToTime } from 'utils/time-utils'
import { SPLIT_JOB_STATISTICS } from '../client-statistics-config'
import { styles } from './client-split-job-modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
   client: ClientAgent
}

/**
 * Component represents a pop-up modal displaying all client job parts
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @param {ClientAgent}[client] - client for which the job was splitted
 * @returns JSX Element
 */
const ClientSplitJobModal = ({ isOpen, setIsOpen, client }: Props) => {
   const { modalStyle } = styles

   const header = [client.name.toUpperCase(), 'JOB PARTS'].join(' ')

   const getSplittedJobs = () => {
      return client.splitJobs.map((job) => {
         const jobTitle = ['JOB', parseSplitJobId(job.splitJobId)].join(' ')
         const parsedJob = Object.assign({}, job, {
            start: convertUnixToTime((job as SplitJob).start),
            end: convertUnixToTime((job as SplitJob).end),
         })
         return (
            <DetailsCard
               {...{
                  title: jobTitle,
                  fieldMap: SPLIT_JOB_STATISTICS,
                  objectMap: parsedJob,
               }}
            />
         )
      })
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
         {getSplittedJobs()}
      </Modal>
   )
}

export default ClientSplitJobModal
