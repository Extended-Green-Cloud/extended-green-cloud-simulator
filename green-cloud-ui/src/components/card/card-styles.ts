import React from "react"

interface Styles {
    cardContainer: React.CSSProperties,
    cardHeader: React.CSSProperties,
    cardContent: React.CSSProperties,
    cardContentScroll: React.CSSProperties
}

export const styles: Styles = {
    cardContainer: {
        display: 'flex',
        flexDirection: 'column',
        padding: '15px',
        boxShadow: 'var(--card-shadow)',
    },
    cardHeader: {
        color: 'var(--gray-2)',
        fontWeight: '300',
    },
    cardContent: {
        flexGrow: 1,
        marginTop: '20px',
        color: 'var(--gray-2)',
        fontWeight: '300',
    },
    cardContentScroll: {
        overflowY: 'scroll',
        overflowX: 'hidden'
    }
}