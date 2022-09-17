import React from 'react'
import { styles } from './banner-styles'
import { iconCloud } from '@assets'

const TopBanner = () => {
    return (
        <div style={styles.parentContainer}>
            <div style={styles.bannerContainer}>
                <img style={styles.bannerIcon} src={iconCloud} alt='Cloud icon' />
                <span style={styles.bannerText}>GREEN CLOUD NETWORK</span>
            </div>
        </div>
    )
}

export default TopBanner