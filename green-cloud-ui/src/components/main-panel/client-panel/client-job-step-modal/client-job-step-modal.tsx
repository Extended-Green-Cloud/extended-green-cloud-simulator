import { JobStep } from '@types'
import { DetailsField } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { styles } from './client-job-step-modal-styles'
import MultiLevelDetailsField from 'components/common/multi-level-detils-field/multi-level-details-field'
import { collectResourcesToMultiMap } from 'utils/resource-utils'
import { CLIENT_STATISTICS_RESOURCES_MAPPER } from '../client-panel-config'
import { convertSecondsToString } from '@utils'
interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
   jobSteps: JobStep[]
}

/**
 * Component represents a pop-up modal displaying statistics of job steps
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @param {JobStep[]}[jobSteps] - job steps
 * @returns JSX Element
 */
const ClientJobStepModal = ({ isOpen, setIsOpen, jobSteps }: Props) => {
   const { modalStyle } = styles

   const header = 'JOB STEPS'

   const getStepsInformation = () => {
      return jobSteps?.map((step) => {
         const resourceMap = step.requiredResources
            ? collectResourcesToMultiMap(step.requiredResources, CLIENT_STATISTICS_RESOURCES_MAPPER)
            : null
         return (
            <>
               <DetailsField label={step.name} isHeader />
               <DetailsField label={'DURATION'} value={convertSecondsToString(step.duration)} />
               {resourceMap && <MultiLevelDetailsField {...{ detailsFieldMap: resourceMap }} />}
            </>
         )
      })
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
         {getStepsInformation()}
      </Modal>
   )
}

export default ClientJobStepModal
