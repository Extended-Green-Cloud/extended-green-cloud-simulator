import React from "react"
import {styles } from './card-styles'

interface Props {
    children?: React.ReactNode,
    header?: string,
    containerStyle?: React.CSSProperties
}

const Card = ({ header, children, containerStyle }: Props) => {
    return (
        <div style={{...styles.cardContainer,...containerStyle}}>
            <div style={styles.cardHeader}>{header?.toUpperCase()}</div>
            <div style={styles.cardContent}>
                {children}
            </div>
        </div>
    )
}

export default Card