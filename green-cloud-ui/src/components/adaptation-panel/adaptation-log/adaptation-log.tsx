import React, { useState } from 'react'

import { styles } from './adaptation-log-styles'
import './css/more-button.css'

import { useAppSelector } from '@store'
import LogEntry from './log-entry/log-entry'
import FullLogModal from './full-log-modal/full-log-modal'

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
               <button
                  className="more-button common-button"
                  onClick={() => setIsOpen((curr) => !curr)}
               >
                  <span>SHOW MORE</span>
                  <span style={styles.buttonIcon}>{'\u00BB'}</span>
               </button>
               <FullLogModal {...{ isOpen, setIsOpen, sortedLogs }} />
            </div>
         )}
      </>
   )
}

export default AdaptationLog
