import React from 'react'
import { styles } from './tab-header-styles'

interface Props {
   tabTitles: string[]
   selectedTabIdx: number
   setSelectedTabIdx: React.Dispatch<React.SetStateAction<number>>
}

/**
 * Component represents header with navigators for tabs
 *
 * @param {string[]}[tabTitles] - list of tab titles
 * @param {number}[selectedTabIdx] - indicator informing which tab is currently selected
 * @param {React.Dispatch<React.SetStateAction<number>>}[setSelectedTabIdx] - function setting the current state of tab selection
 * @returns JSX Element
 */
const TabHeader = ({ tabTitles, selectedTabIdx, setSelectedTabIdx }: Props) => {
   const { header, selectedTab, deselectedTab, commonTab } = styles

   const getHeaderStyle = (tabIndex: number) => ({
      ...commonTab,
      ...(tabIndex === selectedTabIdx ? selectedTab : deselectedTab),
   })

   const generateTabs = () => {
      return tabTitles.map((tab, index) => generateTabHeader(tab, index))
   }

   const generateTabHeader = (headerText: string, index: number) => {
      return (
         <div key={headerText} style={getHeaderStyle(index)} onClick={() => setSelectedTabIdx(index)}>
            {headerText.toUpperCase()}
         </div>
      )
   }

   return <div style={header}>{generateTabs()}</div>
}

export default TabHeader
