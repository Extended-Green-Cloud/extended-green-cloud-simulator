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
    },
    contentContainer: {
        display: 'flex',
        height: '75%',
        minHeight: 0
    },
    leftContentContainer: {
        width: '30%',
        display: 'flex',
        flexDirection: 'column',
        minWidth: 'fit-content'
    }
}