import React from 'react'
import { styles } from './banner-styles'
import { iconCloud } from '@assets'
import './css/banner-button-styles.css'
import { agentsActions, cloudNetworkActions, useAppDispatch, useAppSelector } from '@store'

const header = 'Green cloud network'

/**
 * Component representing the banner displayed at the top of the website
 * 
 * @returns JSX Element 
 */
const TopBanner = () => {
    const dispatch = useAppDispatch()
    const { isServerConnected } = useAppSelector(state => state.cloudNetwork)

    const handleOnReset = () => {
        dispatch(cloudNetworkActions.resetCloudNetwork())
        dispatch(agentsActions.resetAgents())
    }

    const handleOnStop = () => {
        if (isServerConnected) {
            dispatch(cloudNetworkActions.finishNetworkStateFetching())
            dispatch(agentsActions.resetAgents())
        } else {
            dispatch(cloudNetworkActions.startNetworkStateFetching())
        }
    }

    return (
        <div style={styles.parentContainer}>
            <div style={styles.banerContent}>
                <div style={styles.logoContainer}>
                    <img style={styles.bannerIcon} src={iconCloud} alt='Cloud icon' />
                    <span style={styles.bannerText}>{header.toUpperCase()}</span>
                </div>
                <div>
                    <button className='button-banner button-reconnect' onClick={handleOnReset}>
                        {'Reset simulation'.toUpperCase()}
                    </button>
                    <button className='button-banner' onClick={handleOnStop}>
                        {(isServerConnected ? 'Disconnect server' : 'Connect to server').toUpperCase()}
                    </button>
                </div>
            </div>
        </div>
    )
}

export default TopBanner