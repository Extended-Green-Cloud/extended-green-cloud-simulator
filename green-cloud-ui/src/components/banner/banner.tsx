import React, { useState } from 'react'
import { styles } from './banner-styles'
import { iconCloud, iconMenu } from '@assets'
import './css/banner-button-styles.css'
import MenuModal from 'components/menu-modal/menu-modal'

const header = 'Green cloud network'

/**
 * Component representing the banner displayed at the top of the website
 *
 * @returns JSX Element
 */
const TopBanner = () => {
   const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false)

   return (
      <div style={styles.parentContainer}>
         <div style={styles.banerContent}>
            <div style={styles.logoContainer}>
               <img
                  style={styles.bannerCloudIcon}
                  src={iconCloud}
                  alt="Cloud icon"
               />
               <span style={styles.bannerText}>{header.toUpperCase()}</span>
            </div>
            <div>
               <button
                  className="menu-button common-button"
                  onClick={() => setIsMenuOpen(true)}
               >
                  <img
                     style={styles.bannerMenuIcon}
                     src={iconMenu}
                     alt="Menu icon"
                  />
               </button>
            </div>
         </div>
         <MenuModal {...{ isMenuOpen, setIsMenuOpen }} />
      </div>
   )
}

export default TopBanner
