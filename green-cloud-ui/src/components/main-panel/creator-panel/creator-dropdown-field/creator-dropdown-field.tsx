import { Dropdown } from 'components/common'
import { DropdownOption } from '@types'
import { styles } from '../creator-input-field/creator-input-field-styles'

interface Props {
   title: string
   description: string
   selectedData: DropdownOption
   setSelectedData: (option: DropdownOption) => void
   options: DropdownOption[]
   modifyData: any
   wrapperStyle?: React.CSSProperties
}

/**
 * Component represents a dropdown used in creator views
 *
 * @param {string}[title] - title of the dropdown
 * @param {string}[description] - description displayed below dropdown
 * @param {DropdownOption[]}[options] - data that is to be displayed in dropdown
 * @param {any}[modifyData] - function used while modifying the data
 * @param {Dropdown}[selectedData] - selected dropdown option
 * @param {function}[setSelectedData] - function used to update selected option
 * @param {boolean}[wrapperStyle] - optional style that can be applied to wrapper
 *
 * @returns JSX Element
 */
export const CreatorDropdownField = ({
   title,
   description,
   modifyData,
   options,
   selectedData,
   setSelectedData,
   wrapperStyle
}: Props) => {
   const { wrapper, wrapperHeader, wrapperInput, descriptionStyle } = styles
   const styleWrapper = wrapperStyle ? { ...wrapper, ...wrapperStyle } : wrapper

   return (
      <div style={styleWrapper}>
         <div style={wrapperHeader}>{title.toUpperCase()}</div>
         <div style={wrapperInput}>
            <Dropdown
               {...{
                  options: options,
                  value: selectedData,
                  isClearable: false,
                  onChange: (value: any) => {
                     setSelectedData(value)
                     modifyData(value.value)
                  }
               }}
            />
            <div style={descriptionStyle}>{description}</div>
         </div>
      </div>
   )
}
