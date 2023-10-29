import { styles } from './resource-characteristic-field-styles'

interface Props {
   propertyName: string
   children?: React.ReactNode | React.ReactNode[]
}

/**
 * Component representing single resource characteristic field configuration
 *
 * @param {string}[propertyName] - name of the property to be modified
 * @param {React.ReactNode | React.ReactNode[]}[content] - content to be displayed
 * @returns JSX Element
 */
const ResourceCharacteristicField = ({ propertyName, children }: Props) => {
   const { characteristicCommon, textStyle, contentWrapper } = styles

   return (
      <div style={characteristicCommon}>
         <div style={textStyle}>{propertyName.toUpperCase()}</div>
         <div style={contentWrapper}>{children}</div>
      </div>
   )
}

export default ResourceCharacteristicField
