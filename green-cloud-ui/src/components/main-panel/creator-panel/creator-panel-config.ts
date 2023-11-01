import { AgentType, DropdownOption, JobCreator, ResourceMap } from '@types'

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
