import React from "react"

interface Styles {
    cardContainer: React.CSSProperties,
    cardHeader: React.CSSProperties,
    cardContent: React.CSSProperties
}

export const styles: Styles = {
    cardContainer: {
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '3px 3px 13px -3px rgba(0,0,0,0.3)',
    },
    cardHeader: {
        color: 'var(--gray-2)',
        fontWeight: '300',
        marginBottom: '20px',
    },
    cardContent: {
        color: 'var(--gray-2)',
        fontWeight: '300',
        flexGrow: 1
    }
}