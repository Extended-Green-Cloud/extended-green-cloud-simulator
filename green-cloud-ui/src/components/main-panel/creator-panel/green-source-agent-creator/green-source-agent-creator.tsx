import { useState } from 'react'
import { Agent, AgentType, DropdownOption, EnergyType, GreenSourceCreator } from '@types'
import { Dropdown, InputField, SubtitleContainer, UploadJSONButton } from 'components/common'
import { UpdateGreenSourceForm } from '../creator-panel'
import { styles } from './green-source-agent-creator-styles'

interface Props {
   greenSourceAgentData: GreenSourceCreator
   setGreenSourceAgentData: UpdateGreenSourceForm
   agents: Agent[]
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
 *
 * @returns JSX Element
 */
export const GreenSourceAgentCreator = ({ greenSourceAgentData, setGreenSourceAgentData, agents }: Props) => {
   const [selectedServer, setSelectedServer] = useState<DropdownOption>(
      agents.length === 0 ? EMPTY_OPTION : getAvailableServerOptions(agents)[0]
   )
   const [selectedEnergyType, setSelectedEnergyType] = useState<DropdownOption>(getAvailableEnergyOptions()[0])

   const { wrapper, wrapperHeader, wrapperInput, descriptionStyle, container, serverWrapper } = styles

   const updateGreenSourceAgentValue = (newValue: string | number | EnergyType, valueKey: keyof GreenSourceCreator) => {
      setGreenSourceAgentData((prevData) => {
         return {
            ...prevData,
            [valueKey]: newValue
         }
      })
   }

   const getTextInput = (
      title: string,
      description: string,
      fieldName: keyof GreenSourceCreator,
      isNumeric?: boolean,
      isTextField?: boolean
   ) => (
      <div style={wrapper}>
         <div style={wrapperHeader}>{title.toUpperCase()}</div>
         <div style={wrapperInput}>
            <InputField
               {...{
                  value: greenSourceAgentData[fieldName] as string | number,
                  placeholder: description,
                  handleChange: (event: React.ChangeEvent<HTMLInputElement>) =>
                     updateGreenSourceAgentValue(isNumeric ? +event.target.value : event.target.value, fieldName),
                  isNumeric,
                  isTextField
               }}
            />
            <div style={descriptionStyle}>{description}</div>
         </div>
      </div>
   )

   const getServerDropdown = () => (
      <div style={{ ...wrapper, ...serverWrapper }}>
         <div style={wrapperHeader}>SERVER TO CONNECT WITH</div>
         <div style={wrapperInput}>
            <Dropdown
               {...{
                  options: getAvailableServerOptions(agents),
                  value: selectedServer,
                  isClearable: false,
                  onChange: (value: any) => {
                     setSelectedServer(value)
                     updateGreenSourceAgentValue(value.value, 'server')
                  }
               }}
            />
            <div style={descriptionStyle}>Select server with which Green Source is to be connected</div>
         </div>
      </div>
   )

   const getEnergyTypeDropdown = () => (
      <div style={wrapper}>
         <div style={wrapperHeader}>ENERGY TYPE</div>
         <div style={wrapperInput}>
            <Dropdown
               {...{
                  options: getAvailableEnergyOptions(),
                  value: selectedEnergyType,
                  isClearable: false,
                  onChange: (value: any) => {
                     setSelectedEnergyType(value)
                     updateGreenSourceAgentValue(value.value, 'energyType')
                  }
               }}
            />
            <div style={descriptionStyle}>Select type of the Green Source energy</div>
         </div>
      </div>
   )

   return (
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
            {agents.length === 0 ? (
               <SubtitleContainer
                  {...{
                     text: 'Green Energy Source cannot be created because there are no Servers in the systems to which it can be attached'
                  }}
               />
            ) : (
               <>
                  {getServerDropdown()}
                  {getTextInput('Name', 'Provide Green Source name', 'name', false)}
                  {getEnergyTypeDropdown()}
                  {getTextInput('Latitude', 'Provide latitude of given Green Source location', 'latitude', true)}
                  {getTextInput('Longitude', 'Provide longitude of given Green Source location', 'longitude', true)}
                  {getTextInput('Price for power', 'Provide price per power unit', 'pricePerPowerUnit', true)}
                  {getTextInput(
                     'Maximum capacity',
                     'Provide maximum capacity of Green Source',
                     'maximumCapacity',
                     true
                  )}
                  {getTextInput(
                     'Prediction error',
                     'Provide error relevant to weather predictions received by Green Source',
                     'weatherPredictionError',
                     true
                  )}
               </>
            )}
         </div>
      </div>
   )
}
