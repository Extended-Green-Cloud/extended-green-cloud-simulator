import { JobStep } from '@types'
import { DetailsField } from 'components/common'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { styles } from './client-job-step-modal-styles'
import { convertSecondsToString } from '@utils'
import { getJobResourceVal } from 'utils/job-utils'

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
   const { modalStyle, valueStyle, stepWrapper, stepValueContainer, stepValueLabel, stepValue } = styles

   const header = 'JOB STEPS'

   const getStepResourceField = (step: JobStep) => (
      <div style={stepWrapper}>
         <div style={stepValueContainer}>
            <div style={stepValueLabel}>CPU:</div>
            <div style={stepValue}>{getJobResourceVal(step.cpu)}</div>
         </div>
         <div style={stepValueContainer}>
            <div style={stepValueLabel}>MEMORY:</div>
            <div style={stepValue}>{getJobResourceVal(step.memory)}</div>
         </div>
         <div style={stepValueContainer}>
            <div style={stepValueLabel}>DURATION:</div>
            <div style={stepValue}>{convertSecondsToString(step.duration)}</div>
         </div>
      </div>
   )

   const getStepsInformation = () => {
      return jobSteps.map((step) => (
         <DetailsField
            {...{
               key: step.name,
               label: step.name,
               valueObject: getStepResourceField(step),
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
         {getStepsInformation()}
      </Modal>
   )
}

export default ClientJobStepModal
