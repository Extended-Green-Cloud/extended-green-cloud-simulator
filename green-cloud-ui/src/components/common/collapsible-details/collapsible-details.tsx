import Collapse from '../collapse/collapse'
import { styles } from './collapsible-details-styles'
import DetailsField from '../details-field/details-field'
import { MultiLevelValues } from '@types'

interface Props {
   title: string
   fields: MultiLevelValues[]
   disabled?: boolean
}

/**
 * Component represents a collapsible field that contains one-level nested details fields
 *
 * @param {string}[title] - title displayed on the collapsible field
 * @param {string[]}[fields] - set of fields that are to be displayed within the collapsible component
 * @param {boolean}[disabled]
 *
 * @returns JSX.Element
 */
const CollapsibleDetails = ({ title, fields, disabled }: Props) => {
   const { mainFieldWrapper, dropdownTrigger, dropdownContent, dropdownWrapper } = styles

   return (
      <div style={mainFieldWrapper}>
         <Collapse
            title={title.toUpperCase()}
            wrapperStyle={dropdownWrapper}
            contentStyle={dropdownContent}
            triggerStyle={dropdownTrigger}
            disabled={disabled}
         >
            <div>
               {fields.map((field) => (
                  <DetailsField key={field.label} {...{ label: field.label, value: field.value }} />
               ))}
            </div>
         </Collapse>
      </div>
   )
}

export default CollapsibleDetails
