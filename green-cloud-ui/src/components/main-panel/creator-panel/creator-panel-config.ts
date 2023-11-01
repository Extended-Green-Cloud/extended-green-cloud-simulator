/* eslint-disable @typescript-eslint/no-unused-vars */
import { Agent, AgentType, DropdownOption, EnergyType, GreenSourceCreator, JobCreator, ResourceMap } from '@types'
import { validateGreenSourceData, validateNewClientData } from 'utils/agent-creator-utils'

export const AVAILABLE_AGENT_CREATORS: AgentType[] = [AgentType.CLIENT, AgentType.GREEN_ENERGY, AgentType.SERVER]

export const AVAILABLE_AGENT_OPTIONS: DropdownOption[] = Object.values(AgentType)
   .filter((value) => AVAILABLE_AGENT_CREATORS.includes(value as AgentType))
   .map((key) => {
      return { value: key as AgentType, label: key as string, isSelected: false }
   })

export const getEmptyClientForm = (): JobCreator => ({
   processorName: '',
   selectionPreference: '',
   resources: {} as ResourceMap,
   deadline: 0,
   duration: 0,
   steps: []
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

export const CREATOR_CONFIG = {
   [AgentType.CLIENT]: {
      fillWithEmptyData: getEmptyClientForm,
      validateData: (data: any) => validateNewClientData(data)
   },
   [AgentType.GREEN_ENERGY]: {
      fillWithEmptyData: getEmptyGreenSourceForm,
      validateData: (data: any, agents: Agent[]) => validateGreenSourceData(data, agents)
   }
}

export const EMPTY_CREATOR_CONFIG = {
   fillWithEmptyData: () => null,
   validateData: () => ''
}
