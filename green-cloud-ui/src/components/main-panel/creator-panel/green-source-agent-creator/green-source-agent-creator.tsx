import { useState, useEffect } from 'react'
import { Agent, AgentType, DropdownOption, EnergyType, GreenSourceCreator } from '@types'
import { SubtitleContainer, UploadJSONButton } from 'components/common'
import { UpdateGreenSourceForm } from '../creator-panel'
import { styles } from './green-source-agent-creator-styles'
import { CreatorInputField } from '../creator-input-field/creator-input-field'
import { CreatorDropdownField } from '../creator-dropdown-field/creator-dropdown-field'
import { UpdateResourceReset } from 'components/common/resource-form/resource-form'

interface Props {
   greenSourceAgentData: GreenSourceCreator
   setGreenSourceAgentData: UpdateGreenSourceForm
   agents: Agent[]
   resetData: boolean
   setResetData: UpdateResourceReset
}

const getAvailableServerOptions = (agents: Agent[]): DropdownOption[] =>
   agents
      .filter((agent) => agent.type === AgentType.SERVER)
      .map((agent) => agent.name)
      .map((agentName) => {
         return { value: agentName, label: agentName as string, isSelected: false }
      })

export const getAvailableEnergyOptions = (): DropdownOption[] =>
   Object.values(EnergyType).map((key) => {
      return { value: key as EnergyType, label: key as string, isSelected: false }
   })

const EMPTY_OPTION = { value: '', label: '', isSelected: false }

/**
 * Component represents a view allowing to create new green source agent
 *
 * @param {JobCreator}[greenSourceAgentData] - data modified using creator
 * @param {UpdateGreenSourceForm}[setGreenSourceAgentData] - method used to modify green source data
 * @param {boolean}[resetData] - flag indicating if resources should be reset
 * @param {UpdateResourceReset}[setResetData] - method used to modify information if data should be reset
 *
 * @returns JSX Element
 */
export const GreenSourceAgentCreator = ({
   greenSourceAgentData,
   setGreenSourceAgentData,
   agents,
   resetData,
   setResetData
}: Props) => {
   const [selectedServer, setSelectedServer] = useState<DropdownOption>(EMPTY_OPTION)
   const [selectedEnergyType, setSelectedEnergyType] = useState<DropdownOption>(EMPTY_OPTION)

   const { container, serverWrapper } = styles

   useEffect(() => {
      if (resetData) {
         setSelectedServer(EMPTY_OPTION)
         setSelectedEnergyType(EMPTY_OPTION)
         setResetData(false)
      }
   }, [resetData])

   useEffect(() => {
      if (greenSourceAgentData) {
         const server = greenSourceAgentData?.server ?? ''
         const energyType = greenSourceAgentData?.energyType.toString() ?? ''

         setSelectedServer({ label: server, value: server, isSelected: false })
         setSelectedEnergyType({ label: energyType, value: energyType, isSelected: false })
      }
   }, [greenSourceAgentData])

   const updateGreenSourceAgentValue = (newValue: string | number | EnergyType, valueKey: keyof GreenSourceCreator) => {
      setGreenSourceAgentData((prevData) => {
         return {
            ...prevData,
            [valueKey]: newValue
         }
      })
   }

   return (
      <div>
         {agents.filter((agent) => agent.type === AgentType.SERVER).length === 0 ? (
            <SubtitleContainer
               {...{
                  text: 'Green Energy Source cannot be created because there are no Servers in the systems to which it can be attached'
               }}
            />
         ) : (
            <div>
               <UploadJSONButton
                  {...{
                     buttonText: `Upload Green Source configuration`,
                     handleUploadedContent: (data) => {
                        setGreenSourceAgentData(data)
                     }
                  }}
               />
               <div style={container}>
                  <CreatorDropdownField
                     {...{
                        title: 'Server to connect with',
                        description: 'Select server with which Green Source is to be connected',
                        options: getAvailableServerOptions(agents),
                        selectedData: selectedServer,
                        setSelectedData: setSelectedServer,
                        modifyData: (data: any) => updateGreenSourceAgentValue(data, 'server'),
                        wrapperStyle: serverWrapper
                     }}
                  />
                  <CreatorInputField
                     {...{
                        title: 'Name',
                        description: 'Provide Green Source name',
                        fieldName: 'name',
                        dataToModify: greenSourceAgentData,
                        dataModificationFunction: updateGreenSourceAgentValue
                     }}
                  />
                  <CreatorDropdownField
                     {...{
                        title: 'Energy type',
                        description: 'Select type of the Green Source energy',
                        options: getAvailableEnergyOptions(),
                        selectedData: selectedEnergyType,
                        setSelectedData: setSelectedEnergyType,
                        modifyData: (data: any) => updateGreenSourceAgentValue(data, 'energyType')
                     }}
                  />
                  <CreatorInputField
                     {...{
                        title: 'Latitude',
                        description: 'Provide latitude of given Green Source location',
                        fieldName: 'latitude',
                        dataToModify: greenSourceAgentData,
                        dataModificationFunction: updateGreenSourceAgentValue,
                        isNumeric: true
                     }}
                  />
                  <CreatorInputField
                     {...{
                        title: 'Longitude',
                        description: 'Provide longitude of given Green Source location',
                        fieldName: 'longitude',
                        dataToModify: greenSourceAgentData,
                        dataModificationFunction: updateGreenSourceAgentValue,
                        isNumeric: true
                     }}
                  />
                  <CreatorInputField
                     {...{
                        title: 'Price for energy',
                        description: 'Provide price per energy unit',
                        fieldName: 'pricePerPowerUnit',
                        dataToModify: greenSourceAgentData,
                        dataModificationFunction: updateGreenSourceAgentValue,
                        isNumeric: true
                     }}
                  />
                  <CreatorInputField
                     {...{
                        title: 'Maximum capacity',
                        description: 'Provide maximum capacity of Green Source',
                        fieldName: 'maximumCapacity',
                        dataToModify: greenSourceAgentData,
                        dataModificationFunction: updateGreenSourceAgentValue,
                        isNumeric: true
                     }}
                  />
                  <CreatorInputField
                     {...{
                        title: 'Prediction error',
                        description: 'Provide error relevant to weather predictions received by Green Source',
                        fieldName: 'weatherPredictionError',
                        dataToModify: greenSourceAgentData,
                        dataModificationFunction: updateGreenSourceAgentValue,
                        isNumeric: true
                     }}
                  />
               </div>
            </div>
         )}
      </div>
   )
}
