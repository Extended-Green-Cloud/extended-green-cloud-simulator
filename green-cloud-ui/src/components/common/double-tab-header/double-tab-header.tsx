import React from 'react'
import { styles } from './double-tab-header-styles'

interface Props {
   firstTabTitle: string
   secondTabTitle: string
   isFirstTabSelected: boolean
   setIsFirstTabSelected: React.Dispatch<React.SetStateAction<boolean>>
}

/**
 * Component represents header with navigators for two tabs
 *
 * @param {string}[firstTabTitle] - title of the first tab
 * @param {string}[secondTabTitle] - title of the second tab
 * @param {boolean}[isFirstTabSelected] - indicator informing if the first tab is the selected one
 * @param {React.Dispatch<React.SetStateAction<boolean>>}[setIsFirstTabSelected] - function setting the current state of tab selection
 * @returns JSX Element
 */
const DoubleTabHeader = ({
   firstTabTitle,
   secondTabTitle,
   isFirstTabSelected,
   setIsFirstTabSelected,
}: Props) => {
   const { header, selectedTab, deselectedTab, secondTabStyle } = styles

   const getHeaderStyle = (isFirstTab: boolean) => {
      if (isFirstTab) {
         return isFirstTabSelected ? selectedTab : deselectedTab
      }
      return {
         ...secondTabStyle,
         ...(isFirstTabSelected ? deselectedTab : selectedTab),
      }
   }

   const generateTabHeader = (headerText: string) => {
      const isFirstTab = headerText === firstTabTitle
      return (
         <div
            style={getHeaderStyle(isFirstTab)}
            onClick={() => setIsFirstTabSelected(isFirstTab)}
         >
            {headerText.toUpperCase()}
         </div>
      )
   }

   return (
      <div style={header}>
         {generateTabHeader(firstTabTitle)}
         {generateTabHeader(secondTabTitle)}
      </div>
   )
}

export default DoubleTabHeader
