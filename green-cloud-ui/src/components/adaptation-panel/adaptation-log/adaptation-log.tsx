import React, { useState } from 'react'

import { styles } from './adaptation-log-styles'

import LogEntry from './log-entry/log-entry'
import FullLogModal from './full-log-modal/full-log-modal'
import { ModalButton } from 'components/common'
import { AdaptationLog } from '@types'

interface Props {
   sortedLogs: AdaptationLog[]
}

/**
 * Component represents tab summarizing the latest logs regarding the system adaptation
 *
 * @returns JSX Element
 */
export const AdaptationLogPanel = ({ sortedLogs }: Props) => {
   const [isOpen, setIsOpen] = useState(false)
   return (
      <>
         {sortedLogs.length !== 0 && (
            <div>
               <LogEntry adaptationLog={sortedLogs[0]} />
               <ModalButton
                  {...{
                     buttonClassName: 'more-button',
                     setIsOpen,
                     title: (
                        <>
                           <span>SHOW MORE</span>
                           <span style={styles.buttonIcon}>{'\u00BB'}</span>
                        </>
                     ),
                  }}
               />
               <FullLogModal {...{ isOpen, setIsOpen, sortedLogs }} />
            </div>
         )}
      </>
   )
}
