import React, { useEffect } from 'react'
import { styles } from './main-view-style'
import { Menu, GraphPanel, MainPanel, LivePanel } from '@components'
import { MenuTab } from '@types'
import { VIEW_TABS } from './main-view-config'

interface Props {
   selectedTab: MenuTab
   resetServerConnection: () => void
   setSelectedTab: (tab: MenuTab) => void
}

/**
 * Component representing main application view
 *
 * @returns JSX Element
 */
export const MainView = ({ resetServerConnection, setSelectedTab, selectedTab }: Props) => {
   const {
      mainContainer,
      menuContainer,
      contentContainer,
      mainPanelContainer,
      livePanelContainer,
      graphPanelContainer,
      leftSectionContainer,
      rightSectionContainer,
      sectionContainer
   } = styles
   const config = VIEW_TABS[selectedTab]
   const styleMainPanel = config.useDoublePanelView
      ? mainPanelContainer
      : { ...mainPanelContainer, ...{ height: '100%', marginBottom: 0 } }

   useEffect(() => {
      resetServerConnection()
   }, [])

   const changeTab = (tab: MenuTab) => {
      setSelectedTab(tab)
   }

   return (
      <div style={mainContainer}>
         <div style={menuContainer}>
            <Menu {...{ changeTab }} />
         </div>
         <div style={contentContainer}>
            <div style={{ ...sectionContainer, ...leftSectionContainer }}>
               <div style={styleMainPanel}>
                  <MainPanel />
               </div>
               {config.useDoublePanelView && (
                  <div style={livePanelContainer}>
                     <LivePanel />
                  </div>
               )}
            </div>
            <div style={{ ...sectionContainer, ...rightSectionContainer }}>
               <div style={graphPanelContainer}>
                  <GraphPanel />
               </div>
            </div>
         </div>
      </div>
   )
}
