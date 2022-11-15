import React from 'react'

interface Props {
   size: string
   color?: string
}

/**
 * Svg inform icon
 *
 * @param {string}[size] - icon size passed as string (eg. '2px', '10%')
 * @param {string}[color] - optional icon fill
 * @returns JSX object representing svg icon
 */
const IconInfo = ({ size, color }: Props) => {
   const fill = color ?? '#505050'
   return (
      <svg
         xmlns="http://www.w3.org/2000/svg"
         viewBox="0 0 30 30"
         {...{ width: size, height: size }}
      >
         <g id="surface249774927">
            <path
               style={{
                  stroke: 'none',
                  fillRule: 'nonzero',
                  fill,
                  fillOpacity: 1,
               }}
               d="M 15 3 C 8.371094 3 3 8.371094 3 15 C 3 21.628906 8.371094 27 15 27 C 21.628906 27 27 21.628906 27 15 C 27 8.371094 21.628906 3 15 3 Z M 16 21 L 14 21 L 14 14 L 16 14 Z M 15 11.5 C 14.171875 11.5 13.5 10.828125 13.5 10 C 13.5 9.171875 14.171875 8.5 15 8.5 C 15.828125 8.5 16.5 9.171875 16.5 10 C 16.5 10.828125 15.828125 11.5 15 11.5 Z M 15 11.5 "
            />
         </g>
      </svg>
   )
}

export default IconInfo
