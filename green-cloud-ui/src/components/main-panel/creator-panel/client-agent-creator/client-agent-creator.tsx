import { useState } from 'react'
import { JobCreator, ResourceMap } from '@types'
import { Button, InputField, UploadJSONButton } from 'components/common'
import { styles } from './client-agent-creator-styles'
import { ClientAgentCreatorResourceModal } from './client-agent-creator-resource-modal/client-agent-creator-resource-modal'
import { ClientAgentCreatorStepModal } from './client-agent-creator-step-modal/client-agent-creator-step-modal'
import { UpdateClientForm } from '../creator-panel'
import { UpdateResourceReset } from 'components/common/resource-form/resource-form'

interface Props {
   clientAgentData: JobCreator
   setClientAgentData: UpdateClientForm
   resetData: boolean
   setResetData: UpdateResourceReset
}

/**
 * Component represents a view allowing to create new client agent
 *
 * @param {JobCreator}[clientAgentData] - data modified using creator
 * @param {UpdateClientForm}[setClientAgentData] - method used to modify client data
 * @param {boolean}[resetData] - flag indicating if resources should be reset
 * @param {UpdateResourceReset}[setResetData] - method used to modify information if data should be reset
 *
 * @returns JSX Element
 */
export const ClientAgentCreator = ({ setClientAgentData, clientAgentData, resetData, setResetData }: Props) => {
   const [isOpenResources, setIsOpenResources] = useState<boolean>(false)
   const [isOpenSteps, setIsOpenSteps] = useState<boolean>(false)

   const { wrapper, wrapperHeader, wrapperInput, descriptionStyle, modalWrapper } = styles
   const buttonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')

   const updateClientAgentValue = (newValue: string | number | ResourceMap, valueKey: keyof JobCreator) => {
      setClientAgentData((prevData) => {
         return {
            ...prevData,
            [valueKey]: newValue
         }
      })
   }

   const getClientInputField = (
      title: string,
      description: string,
      fieldName: keyof JobCreator,
      isNumeric?: boolean,
      isTextField?: boolean
   ) => (
      <div style={wrapper}>
         <div style={wrapperHeader}>{title.toUpperCase()}</div>
         <div style={wrapperInput}>
            <InputField
               {...{
                  value: clientAgentData[fieldName] as string | number,
                  placeholder: description,
                  handleChange: (event: React.ChangeEvent<HTMLInputElement>) =>
                     updateClientAgentValue(isNumeric ? +event.target.value : event.target.value, fieldName),
                  isNumeric,
                  isTextField
               }}
            />
            <div style={descriptionStyle}>{description}</div>
         </div>
      </div>
   )

   const getResourceInputField = () => (
      <div style={wrapper}>
         <div style={wrapperHeader}>REQUIRED RESOURCES</div>
         <div style={wrapperInput}>
            <Button
               {...{
                  title: 'Specify resources'.toUpperCase(),
                  onClick: () => setIsOpenResources(!isOpenResources),
                  buttonClassName: buttonStyle
               }}
            />
         </div>
      </div>
   )

   const getStepsInputField = () => (
      <div style={wrapper}>
         <div style={wrapperHeader}>JOB STEPS</div>
         <div style={wrapperInput}>
            <Button
               {...{
                  title: 'Specify job steps'.toUpperCase(),
                  onClick: () => setIsOpenSteps(!isOpenSteps),
                  buttonClassName: buttonStyle
               }}
            />
         </div>
      </div>
   )

   return (
      <div>
         <ClientAgentCreatorStepModal
            {...{
               isOpen: isOpenSteps,
               setIsOpen: setIsOpenSteps,
               setClientAgentData,
               initialSteps: clientAgentData.steps,
               resetData,
               setResetData
            }}
         />
         <ClientAgentCreatorResourceModal
            {...{
               isOpen: isOpenResources,
               setIsOpen: setIsOpenResources,
               setClientData: setClientAgentData,
               initialResources: clientAgentData.resources,
               resetData,
               setResetData
            }}
         />
         <UploadJSONButton
            {...{
               buttonText: `Upload Client configuration`,
               handleUploadedContent: (data) => {
                  setClientAgentData(data)
               }
            }}
         />
         <div style={modalWrapper}>
            {getResourceInputField()}
            {getStepsInputField()}
         </div>
         {getClientInputField('Processor type', 'Provide type of task', 'processorName')}
         {getClientInputField(
            'Deadline ',
            'Provide deadline (in hours counted from job creation) of job execution (0 indicates no deadline)',
            'deadline',
            true
         )}
         {getClientInputField(
            'Duration ',
            'Provide duration (in hours counted from job creation) of job execution',
            'duration',
            true
         )}
         {getClientInputField(
            'Preference of server selection ',
            'Optionally provide method using Expression Language that will be used to select the server for job execution',
            'selectionPreference',
            true,
            true
         )}
      </div>
   )
}
