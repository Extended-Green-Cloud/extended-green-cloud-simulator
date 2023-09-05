import { ScheduledJob } from '@types'
import { DetailsCard } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { JOB_FIELD_CONFIG } from '../scheduler-statistics-config'
import { styles } from './scheduler-modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
   jobs?: ScheduledJob[]
}

const header = 'Scheduled jobs'

/**
 * Component represents a pop-up modal displaying all scheduled job in their schedule order
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @returns JSX Element
 */
export const ScheduleModal = ({ isOpen, setIsOpen, jobs }: Props) => {
   const { modalStyle } = styles

   const generateScheduledJobs = () => {
      const arrayCopy = [...(jobs ?? [])].reverse()
      return arrayCopy?.map((job) => {
         const jobTitle = `JOB ${job.jobId}`
         return (
            <DetailsCard
               {...{
                  title: jobTitle,
                  fieldMap: JOB_FIELD_CONFIG,
                  objectMap: job
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
            header: header.toUpperCase()
         }}
      >
         {generateScheduledJobs()}
      </Modal>
   )
}
