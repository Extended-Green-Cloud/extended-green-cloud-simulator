import React, { useState, useEffect } from 'react'
import { ClientCreator, JobStep } from '@types'
import {
   AddWithInput,
   Button,
   Collapse,
   HeaderWithDelete,
   InputField,
   Modal,
   UploadJSONButton
} from 'components/common'
import { styles } from './client-agent-creator-step-modal-styles'
import { ClientAgentCreatorResourceModal } from '../client-agent-creator-resource-modal/client-agent-creator-resource-modal'
import { UpdateResourceReset } from 'components/common/resource-form/resource-form'

interface Props {
   initialSteps: JobStep[]
   isOpen: boolean
   setIsOpen: (isOpen: boolean) => void
   setClientAgentData: (value: React.SetStateAction<ClientCreator>) => void
   resetData: boolean
   setResetData: UpdateResourceReset
}

/**
 * Component represents a modal that allows to fill the information about client job steps
 *
 * @param {JobStep[]}[initialSteps] - initial set of job steps
 * @param {boolean}[isOpen] - flag indicating if modal is open
 * @param {func}[setIsOpen] - function used to open/close the modal
 * @param {func}[setClientAgentData] - function used to update client data
 * @param {boolean}[resetData] - flag indicating if resources should be reset
 * @param {UpdateResourceReset}[setResetData] - method used to modify information if data should be reset
 *
 * @returns JSX Element
 */
export const ClientAgentCreatorStepModal = ({
   initialSteps,
   isOpen,
   setIsOpen,
   setClientAgentData,
   resetData,
   setResetData
}: Props) => {
   const [jobSteps, setJobSteps] = useState<JobStep[]>(initialSteps)
   const [isOpenResources, setIsOpenResources] = useState<boolean[]>(jobSteps.map(() => false))
   const {
      modalContent,
      modalContainer,
      wrapper,
      stepContent,
      stepTrigger,
      stepWrapper,
      stepContentWrapper,
      stepContentWrapperHeader,
      stepContentWrapperInput,
      stepDescriptionStyle
   } = styles

   const closeButtonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')
   const buttonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')

   useEffect(() => {
      if (resetData) {
         setJobSteps([])
         setResetData(false)
      }
   }, [resetData])

   useEffect(() => {
      if (initialSteps) {
         setJobSteps(initialSteps)
      }
   }, [initialSteps])

   const addNewJobStep = (stepName: string) => {
      const newStep: JobStep = { name: stepName, duration: 0, requiredResources: {} }
      setJobSteps((prevState) => prevState.concat([newStep]))
   }

   const deleteJobStep = (stepName: string) => {
      setJobSteps((prevState) => prevState.filter((step) => step.name !== stepName))
   }

   const updateJobStep = (stepName: string, newValue: any, field: keyof JobStep) => {
      setJobSteps((prevState) =>
         prevState.map((step) => (step.name === stepName ? { ...step, [field]: newValue } : step))
      )
   }

   const saveJobSteps = () => {
      setClientAgentData((prevState) => ({
         ...prevState,
         jobCreator: {
            ...prevState.jobCreator,
            steps: jobSteps
         }
      }))
      setIsOpen(!isOpen)
   }

   const getStepFields = () => {
      return jobSteps.map((step, idx) => (
         <Collapse
            {...{
               title: <HeaderWithDelete {...{ title: step.name, deleteFunction: () => deleteJobStep(step.name) }} />,
               triggerStyle: stepTrigger,
               wrapperStyle: stepWrapper,
               contentStyle: stepContent
            }}
         >
            <ClientAgentCreatorResourceModal
               {...{
                  isOpen: isOpenResources[idx],
                  step,
                  setIsOpen: () => setIsOpenResources(jobSteps.map(() => false)),
                  setClientData: setJobSteps,
                  initialResources: step.requiredResources,
                  resetData,
                  setResetData
               }}
            />
            <>
               <div style={stepContentWrapper}>
                  <div style={stepContentWrapperHeader}>REQUIRED STEP RESOURCES</div>
                  <div style={stepContentWrapperInput}>
                     <Button
                        {...{
                           title: 'Specify resources'.toUpperCase(),
                           onClick: () => setIsOpenResources(jobSteps.map((jobStep) => jobStep === step)),
                           buttonClassName: buttonStyle
                        }}
                     />
                  </div>
               </div>
               <div style={stepContentWrapper}>
                  <div style={stepContentWrapperHeader}>{'Step duration'}</div>
                  <div style={stepContentWrapperInput}>
                     <InputField
                        {...{
                           value: step.duration,
                           placeholder: 'Provide duration of step execution',
                           handleChange: (event: React.ChangeEvent<HTMLInputElement>) =>
                              updateJobStep(step.name, +event.target.value, 'duration'),
                           isNumeric: true
                        }}
                     />
                     <div style={stepDescriptionStyle}>{'Provide duration of step execution in seconds'}</div>
                  </div>
               </div>
            </>
         </Collapse>
      ))
   }

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            header: 'Specify client job steps'.toUpperCase(),
            contentStyle: modalContent,
            containerStyle: modalContainer
         }}
      >
         <div style={wrapper}>
            <div>
               <UploadJSONButton {...{ buttonText: 'Upload steps from file', handleUploadedContent: setJobSteps }} />
               <AddWithInput
                  {...{
                     inputTitle: 'Provide name of job step',
                     buttonTitle: 'Add step',
                     handleButtonPress: addNewJobStep
                  }}
               />
               {getStepFields()}
            </div>
            <Button
               {...{
                  title: 'Apply'.toUpperCase(),
                  onClick: saveJobSteps,
                  buttonClassName: closeButtonStyle
               }}
            />
         </div>
      </Modal>
   )
}
