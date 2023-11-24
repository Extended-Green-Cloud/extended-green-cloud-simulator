import React, { useState, useEffect } from 'react'
import { ClientCreator, JobStep, ResourceMap } from '@types'
import { Button, Modal, ResourceForm } from 'components/common'
import { styles } from './client-agent-creator-resource-modal-styles'
import { UpdateResourceReset } from 'components/common/resource-form/resource-form'

interface Props {
   initialResources: ResourceMap
   isOpen: boolean
   step?: JobStep
   setIsOpen: (isOpen: boolean) => void
   setClientData: UpdateJob | UpdateJobStep
   resetData: boolean
   setResetData: UpdateResourceReset
}

type UpdateJob = (value: React.SetStateAction<ClientCreator>) => void
type UpdateJobStep = (value: React.SetStateAction<JobStep[]>) => void

/**
 * Component represents a modal that allows to fill the information about new client job resources
 *
 * @param {ResourceMap}[initialResources] - initial map of resources
 * @param {boolean}[isOpen] - flag indicating if modal is open
 * @param {JobStep}[step] - optional parameter specifying step for which resources are selected
 * @param {func}[setIsOpen] - function used to open/close the modal
 * @param {func}[setClientData] - function used to update resource data
 * @param {boolean}[resetData] - flag indicating if resources should be reset
 * @param {func}[setResetData] - method used to modify information if data should be reset
 *
 * @returns JSX Element
 */
export const ClientAgentCreatorResourceModal = ({
   initialResources,
   isOpen,
   step,
   setIsOpen,
   setClientData,
   resetData,
   setResetData
}: Props) => {
   const [jobResources, setJobResources] = useState<ResourceMap>(initialResources)
   const { modalContent, wrapper, modalContainer } = styles
   const closeButtonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')

   useEffect(() => {
      if (resetData) {
         setJobResources({})
         setResetData(false)
      }
   }, [resetData])

   useEffect(() => {
      if (initialResources && !step) {
         setJobResources(initialResources)
      }
   }, [initialResources])

   const saveJobResources = () => {
      if (!step) {
         const updateData = setClientData as UpdateJob
         updateData((prevState) => ({
            ...prevState,
            jobCreator: {
               ...prevState.jobCreator,
               resources: jobResources
            }
         }))
      } else {
         const updateData = setClientData as UpdateJobStep
         updateData((prevState) =>
            prevState.map((jobStep) =>
               jobStep.name === step.name ? { ...step, requiredResources: jobResources } : step
            )
         )
      }
      setIsOpen(!isOpen)
   }

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            header: `Specify resources for new client job${step?.name ?? ''}`.toUpperCase(),
            contentStyle: modalContent,
            containerStyle: modalContainer
         }}
      >
         <div style={wrapper}>
            <div>
               <ResourceForm
                  {...{
                     newResources: jobResources,
                     setNewResources: setJobResources,
                     skipEmptyResource: true,
                     skipFunctionDefinition: true,
                     resetResource: resetData,
                     setResetResource: setResetData
                  }}
               />
            </div>
            <Button
               {...{
                  title: 'Apply'.toUpperCase(),
                  onClick: saveJobResources,
                  buttonClassName: closeButtonStyle
               }}
            />
         </div>
      </Modal>
   )
}
