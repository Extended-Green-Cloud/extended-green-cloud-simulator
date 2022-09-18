import React from "react"
import { styles } from './card-styles'

interface Props {
    children?: React.ReactNode,
    header?: string | React.ReactNode,
    removeScroll?: boolean,
    containerStyle?: React.CSSProperties
}

const Card = ({ header, children, containerStyle, removeScroll }: Props) => {
    const contentStyle = removeScroll ?
        { ...styles.cardContent } :
        { ...styles.cardContent, ...styles.cardContentScroll }

    const mapHeader = () => {
        if (header) {
            return typeof header === 'string' ?
                <div style={styles.cardHeader}>{header?.toUpperCase()}</div> :
                header
        }
    }

    return (
        <div style={{ ...styles.cardContainer, ...containerStyle }}>
            {mapHeader()}
            <div style={contentStyle}>
                {children}
            </div>
        </div>
    )
}

export default Card