import { useState, useEffect } from 'react'
import { AgentType, DropdownOption, JobCreator } from '@types'
import { Button, Dropdown, ErrorMessage, SubtitleContainer } from 'components/common'
import React from 'react'
import { AVAILABLE_AGENT_OPTIONS, getEmptyClientForm } from './creator-panel-config'
import { styles } from './creator-panel-styles'
import { ClientAgentCreator } from './client-agent-creator/client-agent-creator'
import { validateNewClientData } from 'utils/agent-creator-utils'

interface Props {
   createClient: (jobData: JobCreator) => void
}

export type UpdateClientForm = (value: React.SetStateAction<JobCreator>) => void

/**
 * Component represents a panel allowing to create new agents in the system
 *
 * @param {Agent[]} agents - agents present in the system
 * @param {func} createClient - function used to create client agent
 *
 * @returns JSX Element
 */
export const CreatorPanel = ({ createClient }: Props) => {
   const [selectedAgentType, setSelectedAgentType] = useState<DropdownOption>(AVAILABLE_AGENT_OPTIONS[0])
   const [agentCreator, setAgentCreator] = useState<AgentType | null>(AgentType.CLIENT)
   const [agentCreatorData, setAgentCreatorData] = useState<JobCreator | null>(getEmptyClientForm())
   const [resetData, setResetData] = useState<boolean>(false)
   const [errorText, setErrorText] = useState<string>('')

   const { selectorWrapper, buttonWrapper, creatorHeader, creatorContent, wrapper, content } = styles

   const buttonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')
   const resetButtonStyle = ['large-gray-button', 'large-gray-button-active', 'full-width-button'].join(' ')

   useEffect(() => {
      setErrorText('')
   }, [agentCreatorData])

   const getEmptyCreatorData = (value: DropdownOption) => {
      if (value.value === AgentType.CLIENT) {
         setAgentCreatorData(getEmptyClientForm())
      }
   }

   const validate = () => {
      if (agentCreator === AgentType.CLIENT) {
         return validateNewClientData(agentCreatorData as JobCreator)
      }
      return ''
   }

   const resetCreatorData = () => {
      if (agentCreator === AgentType.CLIENT) {
         setAgentCreatorData(getEmptyClientForm())
         setResetData(true)
      } else {
         setAgentCreatorData(null)
      }
   }

   const createAgent = () => {
      const error = validate()

      setErrorText(error)
      if (error === '') {
         if (agentCreator === AgentType.CLIENT) {
            createClient(agentCreatorData as JobCreator)
         }
      }
   }

   const getCreatorView = () => {
      if (agentCreator === AgentType.CLIENT) {
         return (
            <ClientAgentCreator
               {...{
                  clientAgentData: agentCreatorData as JobCreator,
                  setClientAgentData: setAgentCreatorData as UpdateClientForm,
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
               onChange: (value: any) => {
                  setSelectedAgentType(value)
                  setAgentCreator(value.value as AgentType)
                  getEmptyCreatorData(value as DropdownOption)
               }
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
                              buttonClassName: resetButtonStyle
                           }}
                        />
                     </div>
                     <div style={buttonWrapper}>
                        <Button
                           {...{
                              title: 'Create agent'.toUpperCase(),
                              onClick: () => createAgent(),
                              buttonClassName: buttonStyle
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
