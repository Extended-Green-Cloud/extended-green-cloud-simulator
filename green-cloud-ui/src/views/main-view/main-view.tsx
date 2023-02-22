import React, { useEffect } from 'react'
import { styles } from './main-view-style'
import { Banner, AgentSystemPanel, GraphPanel, AdaptationPanel } from '@components'

interface Props {
   openServerConnection: () => void
}

/**
 * Component representing main application view
 *
 * @returns JSX Element
 */
export const MainView = ({ openServerConnection }: Props) => {
   useEffect(() => {
      openServerConnection()
   })

   return (
      <div style={styles.mainContainer}>
         <Banner />
         <div style={styles.contentContainer}>
            <div style={styles.leftContentContainer}>
               <AgentSystemPanel />
            </div>
            <GraphPanel />
            <div style={styles.rightContentContainer}>
               <AdaptationPanel />
            </div>
         </div>
      </div>
   )
}
