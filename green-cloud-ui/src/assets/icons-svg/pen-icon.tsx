import React from 'react'
import { IconProps } from '@types'

/**
 * Svg pen icon
 *
 * @param {string}[size] - icon size passed as string (eg. '2px', '10%')
 * @param {string}[color] - optional icon fill
 * @returns JSX object representing svg icon
 */
const IconPen = ({ size, color }: IconProps) => {
   const fill = color ?? '#ffffff'
   return (
      <svg
         xmlns="http://www.w3.org/2000/svg"
         viewBox="0 0 423.42 423.421"
         {...{ width: size, height: size, fill, fillOpacity: 0.9 }}
      >
         <g>
            <path
               d="M14.968,329.597l78.703,78.642L8.052,422.804c-4.468,0.796-8.751-3.488-7.956-8.017L14.968,329.597z M32.349,310.747
          l80.845,80.845l121.115-121.114l-80.846-80.845L32.349,310.747z M410.994,13.499l-0.551-0.551
          c-15.545-15.545-39.597-16.646-53.733-2.51l-20.563,20.563l56.794,56.794l20.562-20.563
          C427.64,53.096,426.539,29.044,410.994,13.499z M310.748,41.834c-2.631-2.631-6.671-2.814-9.058-0.428L284.676,58.42
          l-20.869-20.869l8.018-8.018c5.019-5.019,5.019-13.158,0-18.115c-5.019-5.019-13.158-5.019-18.115,0L115.03,149.975
          c-5.019,5.019-5.019,13.158,0,18.115c5.019,5.019,13.158,5.019,18.115,0l97.921-97.92l20.869,20.869l-80.295,80.417l80.846,80.845
          l129.988-130.05c2.387-2.387,2.203-6.426-0.429-9.058L310.748,41.834z"
            />
         </g>
      </svg>
   )
}

export default IconPen
