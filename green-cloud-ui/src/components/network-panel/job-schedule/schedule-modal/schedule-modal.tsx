import { useAppSelector } from '@store'
import { ClientAgent, Job, SplitJob } from '@types'
import { DetailsCard } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { parseSplitJobId, retrieveOriginalJobId } from 'utils/string-utils'
import { convertUnixToTime } from 'utils/time-utils'
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
   const { modalStyle } = styles

   const generateScheduledJobs = () => {
      const arrayCopy = [...(jobs ?? [])].reverse()
      return arrayCopy?.map((jobId) => {
         const client = clients.find(
            (client) => client.job.jobId === retrieveOriginalJobId(jobId)
         )
         if (client) {
            const { isSplit } = client
            const jobTitle = getJobTitle(client, jobId)
            const job = getJob(client, jobId)
            const parsedJob = getJobFields(isSplit, job)
            return (
               <DetailsCard
                  {...{
                     title: jobTitle,
                     fieldMap: JOB_FIELD_CONFIG,
                     objectMap: parsedJob,
                  }}
               />
            )
         }
      })
   }

   const getJobTitle = (client: ClientAgent, jobId: string) => {
      const { isSplit } = client
      return ['JOB', isSplit ? parseSplitJobId(jobId) : jobId].join(' ')
   }

   const getJob = (client: ClientAgent, jobId: string) => {
      return client.isSplit
         ? client.splitJobs.find((splitJob) => splitJob.splitJobId === jobId)
         : client.job
   }

   const getJobFields = (isSplit: boolean, job?: SplitJob | Job) => {
      return !isSplit
         ? job
         : Object.assign({}, job, {
              start: convertUnixToTime((job as SplitJob).start),
              end: convertUnixToTime((job as SplitJob).end),
           })
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
