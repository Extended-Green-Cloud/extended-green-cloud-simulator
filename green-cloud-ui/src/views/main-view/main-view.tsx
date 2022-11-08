import React, { useEffect } from 'react'
import { styles } from './main-view-style'
import {
   AgentStatisticsPanel,
   Banner,
   ClientPanel,
   CloudPanel,
   EventPanel,
   GraphPanel,
} from '@components'
import { cloudNetworkActions, useAppDispatch } from '@store'

/**
 * Component representing main application view
 *
 * @returns JSX Element
 */
const MainView = () => {
   const dispatch = useAppDispatch()

   useEffect(() => {
      dispatch(cloudNetworkActions.startNetworkStateFetching())
   })

   return (
      <div style={styles.mainContainer}>
         <Banner />
         <div style={styles.contentContainer}>
            <div style={styles.leftContentContainer}>
               <CloudPanel />
               <AgentStatisticsPanel />
            </div>
            <GraphPanel />
            <div style={styles.rightContentContainer}>
               <ClientPanel />
               <EventPanel />
            </div>
         </div>
      </div>
   )
}

export default MainView
