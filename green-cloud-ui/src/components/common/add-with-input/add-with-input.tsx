import React, { useState } from 'react'
import { Button, InputField } from 'components/common'
import { styles } from './add-with-input-styles'

interface Props {
   handleButtonPress: (input: string) => void
   buttonTitle: string
   inputTitle: string
   isDisabled?: (input: string) => boolean
}

/**
 * Component representing form with input field and apply button
 *
 * @param {func}[handleButtonPress] - function executed when button is pressed
 * @param {UpdateResource}[buttonTitle] - title displayed on the button
 * @param {UpdateNumeric}[inputTitle] - title displayed as placeholder of input field
 * @param {func}[isDisabled] - additional verification if button should be disabled
 *
 * @returns JSX Element
 */
const AddWithInput = ({ handleButtonPress, buttonTitle, inputTitle, isDisabled }: Props) => {
   const [input, setInput] = useState<string>('')
   const { wrapper, textInput, button } = styles
   const addButton = ['medium-green-button-active', 'medium-green-button', 'full-width-button'].join(' ')
   const addButtonDisabled = ['medium-green-button-disabled', 'medium-green-button', 'full-width-button'].join(' ')

   const isNameEmpty = input === ''
   const isButtonDisabled = isDisabled !== undefined ? isDisabled(input) || isNameEmpty : isNameEmpty

   return (
      <div style={wrapper}>
         <div style={textInput}>
            <InputField
               {...{
                  placeholder: inputTitle,
                  value: input,
                  handleChange: (event: React.ChangeEvent<HTMLInputElement>) => setInput(event.target.value)
               }}
            />
         </div>
         <div style={button}>
            <Button
               {...{
                  title: buttonTitle.toUpperCase(),
                  onClick: () => {
                     handleButtonPress(input)
                     setInput('')
                  },
                  buttonClassName: isButtonDisabled ? addButtonDisabled : addButton,
                  isDisabled: isButtonDisabled
               }}
            />
         </div>
      </div>
   )
}

export default AddWithInput
