import React, { useEffect } from 'react'
import { styles } from './main-view-style'
import {
   StatisticsPanel,
   Banner,
   NetworkPanel,
   GraphPanel,
   AdaptationPanel,
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
               <NetworkPanel />
               <StatisticsPanel />
            </div>
            <GraphPanel />
            <div style={styles.rightContentContainer}>
               <AdaptationPanel />
            </div>
         </div>
      </div>
   )
}

export default MainView
