import React from 'react'
import { styles } from './value-indicator-styles'
import { IconElement } from '@types'

interface Props {
   title: string
   value: number | string
   icon?: IconElement
   color?: string
   wrapperStyle?: React.CSSProperties
}
/**
 * Component represents a container that displays information about particular value and
 * optionally, a relevant icon
 *
 * @param {number}[value] - value
 * @param {string}[title] - title displayed over the indicator
 * @param {React.ElementType}[icon] - icon dispayed next to the value
 * @param {string}[color] - optional icon color
 * @param {React.CSSProperties}[wrapperStyle] - optional additional styling of the wrapper
 * @returns JSX Element
 */
const ValueIndicator = ({ title, value, icon, color, wrapperStyle }: Props) => {
   const Icon = icon
   const { wrapper, container, titleStyle, valueStyle, iconStyle } = styles
   const width = icon ? '50%' : '80%'

   const styleValue = { ...valueStyle, width }
   const styleIcon = color ? { ...iconStyle, color } : iconStyle
   const styleWrapper = wrapperStyle ? { ...wrapper, ...wrapperStyle } : wrapper

   return (
      <div style={styleWrapper}>
         <div style={titleStyle}>{title.toUpperCase()}</div>
         <div style={container}>
            <div style={styleValue}>{value}</div>
            {Icon && <Icon {...styleIcon} />}
         </div>
      </div>
   )
}

export default ValueIndicator
