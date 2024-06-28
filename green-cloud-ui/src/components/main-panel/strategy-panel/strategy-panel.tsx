import { CollapsibleDetails, DetailsField } from 'components/common'
import { STRATEGY_INFORMATION } from './strategy-panel-config'
import { CloudNetworkStatistics, MultiLevelValues } from '@types'

interface Props {
   cloudStatistics: CloudNetworkStatistics
}

export const StrategyPanel = ({ cloudStatistics }: Props) => {
   const mapStatistics = () => {
      return STRATEGY_INFORMATION.map((field) => {
         const { key, label, validator, parseValue } = field
         const value = { ...cloudStatistics }[key] ?? ''

         return mapToFieldObject(key, label, parseValue(value), !validator(value as any))
      })
   }

   const mapToFieldObject = (key: string, label: string, value: any, disabled: boolean) => {
      if (key === 'modifications')
         return <CollapsibleDetails {...{ title: label, fields: mapModifications(value as string[]), disabled }} />
      return <DetailsField {...{ label, value, key }} />
   }

   const mapModifications = (modifications: string[]) =>
      modifications.map(
         (modification: string, idx: number): MultiLevelValues => ({
            label: `Modification ${idx + 1}:`,
            value: modification
         })
      )

   return <div>{mapStatistics()}</div>
}
