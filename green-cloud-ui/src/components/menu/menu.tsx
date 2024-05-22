import React, { useState } from 'react'
import { styles } from './menu-styles'
import { iconCloud, IconMenu } from '@assets'
import MenuModal from 'components/menu/menu-modal/menu-modal-connected'
import { ModalButton } from 'components/common'
import MenuElement from './menu-element/menu-element'
import { ICON_SIZE, MENU_BUTTONS } from './menu-config'
import { MenuTab } from '@types'

const header = 'Extended Green Cloud Simulator'

interface Props {
   changeTab: (tab: MenuTab) => void
}

/**
 * Component representing the banner displayed at the top of the website
 *
 * @param {func}[changeTab] - function that changes displayed tabs
 * @returns JSX Element
 */
const Menu = ({ changeTab }: Props) => {
   const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false)
   const { menuContent, parentContainer, menuCloudIcon, menuHeader, menuContainer } = styles

   const getMenuButtons = () =>
      MENU_BUTTONS.map((button) => (
         <MenuElement
            {...{
               key: button.id,
               header: button.header,
               icon: button.icon,
               iconOffset: button.iconOffset,
               id: button.id,
               changeTab
            }}
         />
      ))

   return (
      <div style={parentContainer}>
         <div style={menuContent}>
            <div style={styles.logoContainer}>
               <img style={menuCloudIcon} src={iconCloud} alt="Cloud logo icon" />
               <span style={menuHeader}>{header.toUpperCase()}</span>
               <div style={menuContainer}>{getMenuButtons()}</div>
            </div>
            <ModalButton
               {...{
                  buttonClassName: 'menu-button',
                  setIsOpen: setIsMenuOpen,
                  title: (
                     <div>
                        <IconMenu size={ICON_SIZE} />
                     </div>
                  )
               }}
            />
         </div>
         <MenuModal {...{ isMenuOpen, setIsMenuOpen }} />
      </div>
   )
}

export default Menu
