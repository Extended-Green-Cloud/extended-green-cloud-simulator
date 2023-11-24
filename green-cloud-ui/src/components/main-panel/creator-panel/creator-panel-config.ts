/* eslint-disable @typescript-eslint/no-unused-vars */
import {
   Agent,
   AgentType,
   ClientCreator,
   DropdownOption,
   EnergyType,
   GreenSourceCreator,
   ResourceMap,
   ServerCreator
} from '@types'
import { validateGreenSourceData, validateNewClientData, validateServerData } from 'utils/agent-creator-utils'

export const AVAILABLE_AGENT_CREATORS: AgentType[] = [AgentType.CLIENT, AgentType.GREEN_ENERGY, AgentType.SERVER]

export const AVAILABLE_AGENT_OPTIONS: DropdownOption[] = Object.values(AgentType)
   .filter((value) => AVAILABLE_AGENT_CREATORS.includes(value as AgentType))
   .map((key) => {
      return { value: key as AgentType, label: (key as string).replaceAll('_', ' ') as string, isSelected: false }
   })

export const getEmptyClientForm = (): ClientCreator => ({
   jobCreator: {
      processorName: '',
      selectionPreference: '',
      resources: {} as ResourceMap,
      deadline: 0,
      duration: 0,
      steps: []
   },
   clientName: ''
})

export const getEmptyGreenSourceForm = (): GreenSourceCreator => ({
   name: '',
   server: '',
   latitude: 0,
   longitude: 0,
   pricePerPowerUnit: 0,
   weatherPredictionError: 0.2,
   maximumCapacity: 0,
   energyType: EnergyType.WIND
})

export const getEmptyServerForm = (): ServerCreator => ({
   name: '',
   cloudNetwork: '',
   maxPower: 0,
   idlePower: 0,
   price: 0,
   jobProcessingLimit: 0,
   resources: {}
})

export const CREATOR_CONFIG = {
   [AgentType.CLIENT]: {
      fillWithEmptyData: getEmptyClientForm,
      validateData: (data: any) => validateNewClientData(data),
      isButtonDisabled: () => false
   },
   [AgentType.GREEN_ENERGY]: {
      fillWithEmptyData: getEmptyGreenSourceForm,
      validateData: (data: any, agents: Agent[]) => validateGreenSourceData(data, agents),
      isButtonDisabled: (agents: Agent[]) => agents.filter((agent) => agent.type === AgentType.SERVER).length === 0
   },
   [AgentType.SERVER]: {
      fillWithEmptyData: getEmptyServerForm,
      validateData: (data: any, agents: Agent[]) => validateServerData(data, agents),
      isButtonDisabled: (agents: Agent[]) =>
         agents.filter((agent) => agent.type === AgentType.CLOUD_NETWORK).length === 0
   }
}

export const EMPTY_CREATOR_CONFIG = {
   fillWithEmptyData: () => null,
   validateData: () => '',
   isButtonDisabled: (agents: Agent[]) => true
}
