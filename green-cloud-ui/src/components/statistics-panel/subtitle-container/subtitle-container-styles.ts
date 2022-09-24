import React from "react"

interface Styles {
    descriptionContainer: React.CSSProperties,
    descriptionText: React.CSSProperties
}

export const styles: Styles = {
    descriptionContainer: {
        width: '70%',
        borderTop: 'var(--border-subtitle)',
        paddingTop: '15px',
        paddingBottom: '10px',
        display: 'flex'
    },
    descriptionText: {
        fontSize: '0.7rem',
        marginLeft: '7px'
    }
}
