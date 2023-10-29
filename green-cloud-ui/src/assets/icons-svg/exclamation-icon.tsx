import React from 'react'
import { IconProps } from '@types'

/**
 * Svg exclamation icon
 *
 * @param {string}[size] - icon size passed as string (eg. '2px', '10%')
 * @param {string}[color] - optional icon fill
 * @returns JSX object representing svg icon
 */
const IconExclamation = ({ size, color }: IconProps) => {
   const fill = color ?? '#000000'
   return (
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" {...{ width: size, height: size }}>
         <path
            fill={fill}
            fill-rule="evenodd"
            d="M10 3a7 7 0 100 14 7 7 0 000-14zm-9 7a9 9 0 1118 0 9 9 0 01-18 0zm10.01 4a1 1 0 01-1 1H10a1 1 0 110-2h.01a1 1 0 011 1zM11 6a1 1 0 10-2 0v5a1 1 0 102 0V6z"
         />
      </svg>
   )
}

export default IconExclamation
