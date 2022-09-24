import React from 'react'
import { styles } from './badge-styles'

interface Props {
    text: string,
    isActive?: boolean
}

const Badge = ({ text, isActive }: Props) => {
    const badgeStyle = isActive ?
        styles.activeBadge :
        styles.inActiveBadge
    const style = { ...styles.badge, ...badgeStyle }

    return (<span {...{ style }}>{text}</span>)
}

export default Badge