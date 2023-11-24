import React, { useState } from 'react'
import { Agent, AgentType, ClientCreator, DropdownOption, GreenSourceCreator, ServerCreator } from '@types'
import { Button, Dropdown, ErrorMessage, SubtitleContainer } from 'components/common'
import {
   AVAILABLE_AGENT_OPTIONS,
   CREATOR_CONFIG,
   EMPTY_CREATOR_CONFIG,
   getEmptyClientForm
} from './creator-panel-config'
import { styles } from './creator-panel-styles'
import { ClientAgentCreator } from './client-agent-creator/client-agent-creator'
import { GreenSourceAgentCreator } from './green-source-agent-creator/green-source-agent-creator'
import { ServerAgentCreator } from './server-agent-creator/server-agent-creator'

interface Props {
   agents: Agent[]
   createClient: (jobData: ClientCreator) => void
   createGreenSource: (greenSourceData: GreenSourceCreator) => void
   createServer: (serverData: ServerCreator) => void
}

export type UpdateClientForm = (value: React.SetStateAction<ClientCreator>) => void
export type UpdateGreenSourceForm = (value: React.SetStateAction<GreenSourceCreator>) => void
export type UpdateServerForm = (value: React.SetStateAction<ServerCreator>) => void

export type Creator = ClientCreator | GreenSourceCreator | ServerCreator | null

/**
 * Component represents a panel allowing to create new agents in the system
 *
 * @param {Agent[]} agents - agents present in the system
 * @param {func} createClient - function used to create client agent
 * @param {func} createGreenSource - function used to create green source agent
 * @param {func} createServer - function used to create server agent
 *
 * @returns JSX Element
 */
export const CreatorPanel = ({ createClient, createGreenSource, createServer, agents }: Props) => {
   const [selectedAgentType, setSelectedAgentType] = useState<DropdownOption>(AVAILABLE_AGENT_OPTIONS[0])
   const [agentCreator, setAgentCreator] = useState<AgentType | null>(AgentType.CLIENT)
   const [agentCreatorData, setAgentCreatorData] = useState<Creator>(getEmptyClientForm())
   const [resetData, setResetData] = useState<boolean>(false)
   const [errorText, setErrorText] = useState<string>('')

   const { selectorWrapper, buttonWrapper, creatorHeader, creatorContent, wrapper, content } = styles

   const buttonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')
   const resetButtonStyle = ['large-gray-button', 'large-gray-button-active', 'full-width-button'].join(' ')

   const buttonStyleDisabled = ['large-green-button', 'large-green-button-disabled', 'full-width-button'].join(' ')
   const resetButtonStyleDisabled = ['large-gray-button', 'large-gray-button-disabled', 'full-width-button'].join(' ')

   const creatorConfig = agentCreator ? CREATOR_CONFIG[agentCreator as AgentType] : EMPTY_CREATOR_CONFIG
   const isDisabled = creatorConfig.isButtonDisabled(agents)

   const resetCreatorData = () => {
      setErrorText('')
      setAgentCreatorData(creatorConfig.fillWithEmptyData())
      setResetData(true)
   }

   const changeCreator = (value: any) => {
      setErrorText('')
      setSelectedAgentType(value)
      setAgentCreatorData(CREATOR_CONFIG[value.value as AgentType].fillWithEmptyData())
      setAgentCreator(value.value as AgentType)
   }

   const createAgent = () => {
      setErrorText('')
      const error = creatorConfig.validateData(agentCreatorData, agents)
      setErrorText(error)

      if (error === '') {
         if (agentCreator === AgentType.CLIENT) {
            createClient(agentCreatorData as ClientCreator)
         }
         if (agentCreator === AgentType.GREEN_ENERGY) {
            createGreenSource(agentCreatorData as GreenSourceCreator)
         }
         if (agentCreator === AgentType.SERVER) {
            createServer(agentCreatorData as ServerCreator)
         }
      }
   }

   const getCreatorView = () => {
      if (agentCreator === AgentType.CLIENT) {
         return (
            <ClientAgentCreator
               {...{
                  clientAgentData: agentCreatorData as ClientCreator,
                  setClientAgentData: setAgentCreatorData as UpdateClientForm,
                  resetData,
                  setResetData
               }}
            />
         )
      }
      if (agentCreator === AgentType.GREEN_ENERGY) {
         return (
            <GreenSourceAgentCreator
               {...{
                  greenSourceAgentData: agentCreatorData as GreenSourceCreator,
                  setGreenSourceAgentData: setAgentCreatorData as UpdateGreenSourceForm,
                  agents,
                  resetData,
                  setResetData
               }}
            />
         )
      }
      if (agentCreator === AgentType.SERVER) {
         return (
            <ServerAgentCreator
               {...{
                  serverAgentData: agentCreatorData as ServerCreator,
                  setServerAgentData: setAgentCreatorData as UpdateServerForm,
                  agents,
                  resetData,
                  setResetData
               }}
            />
         )
      }
   }

   return (
      <div style={wrapper}>
         <Dropdown
            {...{
               options: AVAILABLE_AGENT_OPTIONS,
               value: selectedAgentType,
               isClearable: false,
               onChange: changeCreator
            }}
         />
         <div style={content}>
            <div style={selectorWrapper}>
               {!agentCreator ? (
                  <SubtitleContainer {...{ text: 'Select agent type to open creator view' }} />
               ) : (
                  <div>
                     <div style={creatorHeader}>{`${agentCreator.toString().replace('_', ' ')} CREATOR`}</div>
                     <div style={creatorContent}>{getCreatorView()}</div>
                  </div>
               )}
            </div>
            <div>
               <ErrorMessage {...{ errorText, errorType: 'Invalid data' }} />
               {agentCreator && (
                  <div>
                     <div style={buttonWrapper}>
                        <Button
                           {...{
                              title: 'Reset data'.toUpperCase(),
                              onClick: () => resetCreatorData(),
                              isDisabled,
                              buttonClassName: isDisabled ? resetButtonStyleDisabled : resetButtonStyle
                           }}
                        />
                     </div>
                     <div style={buttonWrapper}>
                        <Button
                           {...{
                              title: 'Create agent'.toUpperCase(),
                              onClick: () => createAgent(),
                              isDisabled,
                              buttonClassName: isDisabled ? buttonStyleDisabled : buttonStyle
                           }}
                        />
                     </div>
                  </div>
               )}
            </div>
         </div>
      </div>
   )
}
