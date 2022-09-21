import { IconInfo } from "@assets"
import React from "react"
import { styles } from "./subtitle-container-styles"

interface Props {
    text: string
}

/**
 * Component represents information subtitle that can be placed on the panels
 * 
 * @param {string}[text] - text to be displayed
 * @returns JSX Element 
 */
const SubtitleContainer = ({ text }: Props) => {
    return (
        <div style={styles.descriptionContainer}>
            <IconInfo size="1.2rem" />
            <span style={styles.descriptionText}>
                {text.toUpperCase()}
            </span>
        </div>
    )
}

export default SubtitleContainer