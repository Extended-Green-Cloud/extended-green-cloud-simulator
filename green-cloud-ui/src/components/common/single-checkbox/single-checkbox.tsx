import { styles } from './single-checkbox-styles'
import { Checkbox } from 'pretty-checkbox-react'
import '@djthoms/pretty-checkbox'
import { DropdownOption } from '@types'

interface Props {
   option: DropdownOption
   onChange: (status: string, isSelected: boolean) => void
   disabled?: boolean
}

/**
 * Component representing checkbox with single options
 *
 * @param {DropdownOption}[option] - option data
 * @param {func}[onChange] - function being executed when value of checkbox is changed
 * @param {boolean}[disabled] - flag indicating if check box is disabled
 * @returns JSX Element
 */
const SingleCheckBox = ({ option, onChange, disabled }: Props) => {
   const { checkBoxStyle } = styles
   const { value, isSelected } = option

   return (
      <Checkbox
         {...{
            className: isSelected ? 'checkbox-selected' : '',
            key: value as any,
            value: value as string,
            style: checkBoxStyle,
            checked: isSelected,
            color: 'success',
            shape: 'curve',
            disabled,
            animation: 'smooth',
            onChange: () => onChange(value as string, isSelected as boolean)
         }}
      >
         {value as string}
      </Checkbox>
   )
}

export default SingleCheckBox
