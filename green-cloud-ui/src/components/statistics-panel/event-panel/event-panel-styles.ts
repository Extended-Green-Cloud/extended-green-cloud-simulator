import React from "react"

interface Styles {
    eventContainer: React.CSSProperties,
    singleEventContainer: React.CSSProperties,
    eventTitle: React.CSSProperties,
    singleEventParentContainer: React.CSSProperties
}

export const styles: Styles = {
    eventContainer: {
        backgroundColor: 'var(--white)',
        minWidth: 'fit-content',
        minHeight: '35%'
    },
    singleEventParentContainer: {
        display: 'flex',
        flexDirection: 'column'
    },
    singleEventContainer: {
        display: 'flex',
        flexDirection: 'column',
        padding: '15px 10px',
        margin: '10px',
        borderLeft: 'var(--border-event)',
        boxShadow: 'var(--event-shadow)',
    },
    eventTitle: {
        fontSize: '0.8rem',
        marginBottom: '15px',
        paddingBottom: '5px',
        fontWeight: 400
    }
}
