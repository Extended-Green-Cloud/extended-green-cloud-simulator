import React from 'react'

import { convertUnixToTime } from 'utils/time-utils'
import { AdaptationLog } from '@types'
import { ICON_MAP } from '../adaptation-log-config'
import { styles } from './log-entry-styles'

interface Props {
   adaptationLog: AdaptationLog
}

/**
 * Component represents single field of adaptation log data
 *
 * @param {AdaptationLog}[adaptationLog] - entry of the adaptation log
 * @returns JSX Element
 */
const LogEntry = ({ adaptationLog }: Props) => {
   const {
      wrapperContainer,
      iconContainer,
      iconSize,
      contentContainer,
      descriptionContainer,
      headerContainer,
      agentName,
      dateContainer,
   } = styles
   const icon = { ...(ICON_MAP as any) }[adaptationLog?.type.toString()]
   const date = convertUnixToTime(adaptationLog?.time)

   return (
      <div style={wrapperContainer}>
         <div style={iconContainer}>
            <img {...iconSize} src={icon.icon} alt={icon.alt} />
         </div>
         <div style={contentContainer}>
            <div style={headerContainer}>
               <div style={dateContainer}>{['[', date, ']'].join('')}</div>
               {adaptationLog.agentName && (
                  <span style={agentName}>
                     {adaptationLog.agentName.toUpperCase()}
                  </span>
               )}
            </div>
            <div style={descriptionContainer}>{adaptationLog.description}</div>
         </div>
      </div>
   )
}

export default LogEntry
