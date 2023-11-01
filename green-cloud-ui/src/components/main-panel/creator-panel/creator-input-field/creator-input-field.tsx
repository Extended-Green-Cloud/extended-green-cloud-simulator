import { InputField } from 'components/common'
import { styles } from './creator-input-field-styles'

interface Props {
   title: string
   dataToModify: any
   dataModificationFunction: any
   description: string
   fieldName: string
   isNumeric?: boolean
   isTextField?: boolean
}

/**
 * Component represents an input field used in creator views
 *
 * @param {string}[title] - title of the input field
 * @param {string}[description] - description displayed below input
 * @param {any}[dataToModify] - data that is to be modified
 * @param {any}[dataModificationFunction] - function used while modifying the data
 * @param {string}[fieldName] - name of the field that is to be changed
 * @param {boolean}[isNumeric] - optional flag indicating if the field is to be numeric
 * @param {boolean}[isTextField] - optional flag indicating if the field should be a text area
 *
 * @returns JSX Element
 */
export const CreatorInputField = ({
   title,
   description,
   fieldName,
   isNumeric,
   isTextField,
   dataModificationFunction,
   dataToModify
}: Props) => {
   const { wrapper, wrapperHeader, wrapperInput, descriptionStyle } = styles

   return (
      <div style={wrapper}>
         <div style={wrapperHeader}>{title.toUpperCase()}</div>
         <div style={wrapperInput}>
            <InputField
               {...{
                  value: dataToModify[fieldName] as string | number,
                  placeholder: description,
                  handleChange: (event: React.ChangeEvent<HTMLInputElement>) =>
                     dataModificationFunction(isNumeric ? +event.target.value : event.target.value, fieldName),
                  isNumeric,
                  isTextField
               }}
            />
            <div style={descriptionStyle}>{description}</div>
         </div>
      </div>
   )
}
