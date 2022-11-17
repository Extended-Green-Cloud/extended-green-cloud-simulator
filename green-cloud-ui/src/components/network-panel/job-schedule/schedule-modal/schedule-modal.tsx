import { useAppSelector } from '@store'
import { ClientAgent } from '@types'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { JOB_FIELD_CONFIG } from '../job-schedule-config'
import { styles } from './schedule-modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
}

const header = 'Scheduled jobs'

/**
 * Component represents a pop-up modal displaying all scheduled job in their schedule order
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @returns JSX Element
 */
const ScheduleModal = ({ isOpen, setIsOpen }: Props) => {
   const jobs = useAppSelector(
      (state) => state.cloudNetwork.scheduler?.scheduledJobs
   )
   const clients = useAppSelector(
      (state) => state.agents.clients
   ) as ClientAgent[]
   const {
      modalStyle,
      jobContainer,
      jobHeader,
      jobField,
      jobFieldLabel,
      jobFieldVal,
   } = styles

   const generateScheduledJobs = () => {
      const arrayCopy = [...(jobs ?? [])].reverse()
      return arrayCopy?.map((job) => getJobField(job))
   }

   const parseClient = (label: string, key: string, client: ClientAgent) => {
      const value = { ...(client as any) }[key]
      return (
         <div style={jobField}>
            <div style={jobFieldLabel}>{label}</div>
            <div style={jobFieldVal}>{value}</div>
         </div>
      )
   }

   const getJobField = (jobId: string) => {
      const client = clients.find((client) => client.jobId === jobId)
      const jobTitle = ['JOB', jobId].join(' ')
      return (
         <div style={jobContainer}>
            <div style={jobHeader}>{jobTitle}</div>
            <div>
               {JOB_FIELD_CONFIG.map(
                  (field) =>
                     client && parseClient(field.label, field.key, client)
               )}
            </div>
         </div>
      )
   }

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            contentStyle: modalStyle,
            header: header.toUpperCase(),
         }}
      >
         {generateScheduledJobs()}
      </Modal>
   )
}

export default ScheduleModal
