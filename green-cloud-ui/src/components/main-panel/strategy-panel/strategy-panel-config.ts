import { capitalizeEnum } from '@utils'

interface StrategyInformationConfig {
   key: string
   label: string
   parseValue: (val: string | string[] | number | boolean) => any
   validator: (val?: string[]) => boolean
}

export const STRATEGY_INFORMATION: StrategyInformationConfig[] = [
   {
      key: 'allocationStrategy',
      label: 'Global resource allocation strategy',
      parseValue: (val) => capitalizeEnum(val as string),
      validator: () => true
   },
   {
      key: 'prioritizationStrategy',
      label: 'Global tasks prioritization strategy',
      parseValue: (val) => capitalizeEnum(val as string),
      validator: () => true
   },
   {
      key: 'allocationStepsNumber',
      label: 'Number of allocation steps',
      parseValue: (val) => val,
      validator: () => true
   },
   {
      key: 'modifications',
      label: 'Applied modifications',
      parseValue: (val) => (val as string[]).map((modification) => capitalizeEnum(modification)),
      validator: (modifications) => modifications?.length !== 0
   }
]
