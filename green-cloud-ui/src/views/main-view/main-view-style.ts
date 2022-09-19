import React from "react"

interface Styles {
    mainContainer: React.CSSProperties,
    contentContainer: React.CSSProperties,
    leftContentContainer: React.CSSProperties
}

export const styles: Styles = {
    mainContainer: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
    },
    contentContainer: {
        display: 'flex',
        height: '75%',
        minHeight: 0,
        marginTop: '-4%',
        minWidth: 'fit-content'
    },
    leftContentContainer: {
        width: '30%',
        flexShrink: 0,
        display: 'flex',
        flexDirection: 'column',
        minWidth: 'fit-content'
    }
}