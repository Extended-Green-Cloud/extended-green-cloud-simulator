import React from 'react'
import { styles } from './badge-styles'

interface Props {
   text: string
   isActive?: boolean
}

/**
 * Component representing a text badge
 *
 * @param {string}[text] - text to be displayes inside the badge
 * @param {boolean}[isActive] - (optional) state of the badge
 *
 * @returns JSX Element
 */
const Badge = ({ text, isActive }: Props) => {
   const badgeStyle = isActive ? styles.activeBadge : styles.inActiveBadge
   const style = { ...styles.badge, ...badgeStyle }

   return <span {...{ style }}>{text}</span>
}

export default Badge
