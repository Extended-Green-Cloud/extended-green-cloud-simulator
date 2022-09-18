import React from "react"

interface Styles {
    cardContainer: React.CSSProperties,
    cardHeader: React.CSSProperties,
    cardContent: React.CSSProperties,
    cardContentScroll: React.CSSProperties
}

export const styles: Styles = {
    cardContainer: {
        padding: '15px',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '3px 3px 13px -3px rgba(0,0,0,0.3)',
    },
    cardHeader: {
        color: 'var(--gray-2)',
        fontWeight: '300',
    },
    cardContent: {
        marginTop: '20px',
        color: 'var(--gray-2)',
        fontWeight: '300',
        flexGrow: 1,
    },
    cardContentScroll: {
        overflowY: 'auto',
        overflowX: 'hidden'
    }
}