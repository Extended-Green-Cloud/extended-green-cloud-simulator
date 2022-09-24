import React from "react"

interface Styles {
    mainContainer: React.CSSProperties,
    contentContainer: React.CSSProperties,
    leftContentContainer: React.CSSProperties,
    rightContentContainer: React.CSSProperties
}

export const styles: Styles = {
    mainContainer: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        marginBottom: '5px'
    },
    contentContainer: {
        display: 'flex',
        height: '75%',
        minHeight: 0,
        marginTop: '-7%',
        minWidth: 'fit-content'
    },
    leftContentContainer: {
        width: '30%',
        flexShrink: 0,
        display: 'flex',
        flexDirection: 'column',
        minWidth: 'fit-content'
    },
    rightContentContainer: {
        width: '25%',
        flexShrink: 0,
        display: 'flex',
        height: '100%',
        flexDirection: 'column',
        minWidth: 'fit-content',
        marginRight: '20px'
    }
}