import React from 'react'

import { styles } from './main-panel-styles'

import { Card } from '@components'
import { PANEL_TABS } from './main-panel-config'
import { Agent, MenuTab } from '@types'

interface Props {
   selectedAgent?: Agent | null
   selectedTab: MenuTab
}

/**
 * Component represents a panel gathering all information about cloud network
 *
 * @returns JSX Element
 */
export const MainPanel = ({ selectedTab, selectedAgent }: Props) => {
   const { mainContainer } = styles
   const tab = PANEL_TABS.filter((tab) => tab.id === selectedTab)[0]
   const subHeader = selectedTab === MenuTab.AGENTS ? selectedAgent?.name : undefined

   return (
      <Card
         {...{
            containerStyle: mainContainer,
            header: tab.header,
            subHeader,
            removeScroll: tab.removeScroll
         }}
      >
         {tab.panel}
      </Card>
   )
}

export default MainPanel
