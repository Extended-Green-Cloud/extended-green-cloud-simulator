import React from "react"

interface Styles {
    graphContainer: React.CSSProperties
}

export const styles: Styles = {
    graphContainer: {
        flexGrow: 1,
        height: '105%',
        backgroundColor: 'var(--white)',
        minWidth: 'min-content',
        marginTop: '-1%',
        marginLeft: '20px',
        marginRight: '20px'
    }
}