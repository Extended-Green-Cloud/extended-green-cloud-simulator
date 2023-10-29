import { Collapse } from 'components/common'
import { styles } from './resource-configuration-field-styles'

interface Props {
   fieldName: string
   children?: React.ReactNode | React.ReactNode[]
}

/**
 * Component representing single component field
 *
 * @param {string}[fieldName] - name of the resource field
 * @param {Resource}[resource] - resource which is to be configured
 * @param {React.ReactNode | React.ReactNode[]}[children] - content of the collapse
 *
 * @returns JSX Element
 */
const ResourceConfigurationField = ({ fieldName, children }: Props) => {
   const { fieldWrapper, fieldContent, fieldTrigger } = styles

   return (
      <Collapse
         {...{
            title: fieldName.toUpperCase(),
            wrapperStyle: fieldWrapper,
            triggerStyle: fieldTrigger,
            contentStyle: fieldContent
         }}
      >
         {children}
      </Collapse>
   )
}

export default ResourceConfigurationField
