import React from 'react'
import { styles } from './banner-styles'
import { iconCloud } from '@assets'
import './css/banner-button-styles.css'
import { agentsActions, cloudNetworkActions, useAppDispatch } from '@store'

const header = 'Green cloud network'

/**
 * Component representing the banner displayed at the top of the website
 * 
 * @returns JSX Element 
 */
const TopBanner = () => {
    const dispatch = useAppDispatch()

    const handleOnReset = () => {
        dispatch(cloudNetworkActions.resetCloudNetwork())
        dispatch(agentsActions.resetAgents())
    }

    return (
        <div style={styles.parentContainer}>
            <div style={styles.banerContent}>
                <div style={styles.logoContainer}>
                    <img style={styles.bannerIcon} src={iconCloud} alt='Cloud icon' />
                    <span style={styles.bannerText}>{header.toUpperCase()}</span>
                </div>
                <button className='button-banner' onClick={handleOnReset}>
                    {'Reset simulation'.toUpperCase()}
                </button>
            </div>
        </div>
    )
}

export default TopBanner