import React from 'react'
import { styles } from './banner-styles'
import { iconCloud } from '@assets'

const header = 'Green cloud network'

/**
 * Component representing the banner displayed at the top of the website
 * 
 * @returns JSX Element 
 */
const TopBanner = () => {
    return (
        <div style={styles.parentContainer}>
            <div style={styles.bannerContainer}>
                <img style={styles.bannerIcon} src={iconCloud} alt='Cloud icon' />
                <span style={styles.bannerText}>{header.toUpperCase()}</span>
            </div>
        </div>
    )
}

export default TopBanner