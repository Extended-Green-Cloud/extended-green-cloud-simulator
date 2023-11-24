import { useState } from 'react'
import { ClientCreator, JobCreator, ResourceMap } from '@types'
import { UploadJSONButton } from 'components/common'
import { styles } from './client-agent-creator-styles'
import { ClientAgentCreatorResourceModal } from './client-agent-creator-resource-modal/client-agent-creator-resource-modal'
import { ClientAgentCreatorStepModal } from './client-agent-creator-step-modal/client-agent-creator-step-modal'
import { UpdateClientForm } from '../creator-panel'
import { UpdateResourceReset } from 'components/common/resource-form/resource-form'
import { CreatorInputField } from '../creator-input-field/creator-input-field'
import { CreatorButtonField } from '../creator-button-field/creator-button-field'

interface Props {
   clientAgentData: ClientCreator
   setClientAgentData: UpdateClientForm
   resetData: boolean
   setResetData: UpdateResourceReset
}

/**
 * Component represents a view allowing to create new client agent
 *
 * @param {ClientCreator}[clientAgentData] - data modified using creator
 * @param {UpdateClientForm}[setClientAgentData] - method used to modify client data
 * @param {boolean}[resetData] - flag indicating if resources should be reset
 * @param {UpdateResourceReset}[setResetData] - method used to modify information if data should be reset
 *
 * @returns JSX Element
 */
export const ClientAgentCreator = ({ setClientAgentData, clientAgentData, resetData, setResetData }: Props) => {
   const [isOpenResources, setIsOpenResources] = useState<boolean>(false)
   const [isOpenSteps, setIsOpenSteps] = useState<boolean>(false)
   const { modalWrapper } = styles

   const updateClientAgentValue = (newValue: string | number | ResourceMap, valueKey: keyof ClientCreator) => {
      setClientAgentData((prevData) => {
         return {
            ...prevData,
            [valueKey]: newValue
         }
      })
   }

   const updateClientAgentJobValue = (newValue: string | number | ResourceMap, valueKey: keyof JobCreator) => {
      setClientAgentData((prevData) => {
         return {
            ...prevData,
            jobCreator: {
               ...prevData.jobCreator,
               [valueKey]: newValue
            }
         }
      })
   }

   return (
      <div>
         <ClientAgentCreatorStepModal
            {...{
               isOpen: isOpenSteps,
               setIsOpen: setIsOpenSteps,
               setClientAgentData,
               initialSteps: clientAgentData.jobCreator.steps,
               resetData,
               setResetData
            }}
         />
         <ClientAgentCreatorResourceModal
            {...{
               isOpen: isOpenResources,
               setIsOpen: setIsOpenResources,
               setClientData: setClientAgentData,
               initialResources: clientAgentData.jobCreator.resources,
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
         <CreatorInputField
            {...{
               title: 'Client name',
               description: 'Provide client name',
               fieldName: 'clientName',
               dataToModify: clientAgentData,
               dataModificationFunction: updateClientAgentValue
            }}
         />
         <div style={modalWrapper}>
            <CreatorButtonField
               {...{
                  title: 'Required resources',
                  buttonName: 'Specify resources',
                  onClick: () => setIsOpenResources(!isOpenResources)
               }}
            />
            <CreatorButtonField
               {...{
                  title: 'Job steps',
                  buttonName: 'Specify job steps',
                  onClick: () => setIsOpenSteps(!isOpenSteps)
               }}
            />
         </div>
         <CreatorInputField
            {...{
               title: 'Processor type',
               description: 'Provide type of task',
               fieldName: 'processorName',
               dataToModify: clientAgentData.jobCreator,
               dataModificationFunction: updateClientAgentJobValue
            }}
         />
         <CreatorInputField
            {...{
               title: 'Deadline',
               description:
                  'Provide deadline (in hours counted from job creation) of job execution (0 indicates no deadline)',
               fieldName: 'deadline',
               dataToModify: clientAgentData.jobCreator,
               dataModificationFunction: updateClientAgentJobValue,
               isNumeric: true
            }}
         />
         <CreatorInputField
            {...{
               title: 'Duration',
               description: 'Provide duration (in hours counted from job creation) of job execution',
               fieldName: 'duration',
               dataToModify: clientAgentData.jobCreator,
               dataModificationFunction: updateClientAgentJobValue,
               isNumeric: true
            }}
         />
         <CreatorInputField
            {...{
               title: 'Preference of server selection',
               description: 'Provide method that will be used to select the server for job execution',
               fieldName: 'selectionPreference',
               dataToModify: clientAgentData.jobCreator,
               dataModificationFunction: updateClientAgentJobValue,
               isTextField: true
            }}
         />
      </div>
   )
}
