import { ChangeEventHandler } from 'react'
import './input-field-styles.css'

interface Props {
   label?: string
   placeholder: string
   value?: number | string
   disabled?: boolean
   isNumeric?: boolean
   isTextField?: boolean
   useCodeFormatter?: boolean
   handleChange: ChangeEventHandler<HTMLInputElement> | ChangeEventHandler<HTMLTextAreaElement>
}

/**
 * Component represents field which can be used to take text input
 *
 * @param {string}[label] - label describing the field
 * @param {string}[placeholder] - placeholder describing the field
 * @param {number | undefined}[value] - value being the current input
 * @param {boolean}[disabled] - optional parameter indicating whether the field should be disabled
 * @param {void}[handleChange] - callback function to handle input change
 * @param {boolean}[isNumeric] - flag indicating if input can be only numeric
 * @param {boolean}[isTextField] - flag indicating if the input is a textarea
 * @param {boolean}[useCodeFormatter] - optional flag allowing to format text value in accordance to Java syntax
 * @returns JSX Element
 */
const InputField = ({
   label,
   placeholder,
   value,
   handleChange,
   disabled,
   isNumeric,
   isTextField,
   useCodeFormatter
}: Props) => {
   const textInputStyle = disabled ? 'text-input text-input-inactive' : 'text-input text-input-active'
   const labelStyle = ['text-input-label', disabled ? 'text-input-disabled-label' : 'text-input-active-label'].join(' ')

   const formatValue = (value: string | number) =>
      useCodeFormatter && !isNumeric && typeof value !== 'number'
         ? value
              .split(/;{1}(?=[^\n\r*]{1})/g)
              .map((val) => (val.endsWith('; ') ? val.trim() : val))
              .join(';\n')
         : value

   return (
      <div className="text-input-container">
         <label className={labelStyle}>{label?.toUpperCase()}</label>
         {!isTextField ? (
            <input
               {...{
                  value: formatValue(value ?? ''),
                  onChange: handleChange as ChangeEventHandler<HTMLInputElement>,
                  placeholder,
                  type: !isNumeric ? 'text' : 'number',
                  pattern: isNumeric ? '^[0-9]*[.,]?[0-9]*$' : undefined,
                  className: textInputStyle,
                  disabled
               }}
            />
         ) : (
            <textarea
               {...{
                  value: formatValue(value ?? ''),
                  onChange: handleChange as ChangeEventHandler<HTMLTextAreaElement>,
                  placeholder,
                  className: textInputStyle,
                  disabled
               }}
            />
         )}
      </div>
   )
}

export default InputField
