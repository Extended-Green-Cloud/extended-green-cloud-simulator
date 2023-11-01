import { Button } from 'components/common'
import { styles } from '../creator-input-field/creator-input-field-styles'

interface Props {
   title: string
   onClick: any
   buttonName: string
}

/**
 * Component represents a field with button used in creator views
 *
 * @param {string}[title] - title of the button field
 * @param {string}[buttonName] - text displayed on the button
 * @param {func}[onClick] - function executed on button click
 *
 * @returns JSX Element
 */
export const CreatorButtonField = ({ title, buttonName, onClick }: Props) => {
   const { wrapper, wrapperHeader, wrapperInput } = styles

   const buttonStyle = ['large-green-button', 'large-green-button-active', 'full-width-button'].join(' ')

   return (
      <div style={wrapper}>
         <div style={wrapperHeader}>{title.toUpperCase()}</div>
         <div style={wrapperInput}>
            <Button {...{ title: buttonName.toUpperCase(), onClick, buttonClassName: buttonStyle }} />
         </div>
      </div>
   )
}
