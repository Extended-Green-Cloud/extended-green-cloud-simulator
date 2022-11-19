import React, { useState } from 'react'

import { styles } from './adaptation-log-styles'

import { useAppSelector } from '@store'
import LogEntry from './log-entry/log-entry'
import FullLogModal from './full-log-modal/full-log-modal'
import { ModalButton } from 'components/common'

/**
 * Component represents tab summarizing the latest logs regarding the system adaptation
 *
 * @returns JSX Element
 */
const AdaptationLog = () => {
   const [isOpen, setIsOpen] = useState(false)
   const logs = useAppSelector((state) => state.managingSystem.adaptationLogs)
   const sortedLogs = [...logs].sort((log1, log2) => log2.time - log1.time)

   return (
      <>
         {logs.length !== 0 && (
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

export default AdaptationLog
