import React from "react"

interface Styles {
    mainContainer: React.CSSProperties,
    contentContainer: React.CSSProperties
}

export const styles: Styles = {
    mainContainer: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
    },
    contentContainer: {
        display: 'flex',
        flexGrow: 1,
    }
}