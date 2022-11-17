import React from 'react'

import { styles } from './adaptation-panel-styles'
import { Card, EventPanel, Collapse } from '@components'
import { INITIAL_TABS, TabProps } from './adaptation-panel-config'
import AdaptationStatistics from './adaptation-statistics/adaptation-statistics'
import AdaptationLog from './adaptation-log/adaptation-log'
import AdaptationGoals from './adaptation-goals/adaptation-goals'

const header = 'Adaptation Panel'

/**
 * Component represents a panel gathering all tabs related to the system's adaptations
 *
 * @returns JSX Element
 */
const AdaptationPanel = () => {
   const { tabHeader, adaptationContainer } = styles

   const getTabComponent = (tab: TabProps) => {
      switch (tab.title) {
         case 'Adaptation Statistics':
            return <AdaptationStatistics />
         case 'Adaptation Log':
            return <AdaptationLog />
         case 'Adaptation Goals':
            return <AdaptationGoals />
         case 'Event Triggers':
            return <EventPanel />
      }
   }

   const generateTabs = () =>
      INITIAL_TABS.map((tab) => {
         const title = tab.title.toUpperCase()
         return (
            <Collapse {...{ title, triggerStyle: tabHeader, key: title }}>
               {getTabComponent(tab)}
            </Collapse>
         )
      })

   return (
      <Card {...{ header, containerStyle: adaptationContainer }}>
         {generateTabs()}
      </Card>
   )
}

export default AdaptationPanel
