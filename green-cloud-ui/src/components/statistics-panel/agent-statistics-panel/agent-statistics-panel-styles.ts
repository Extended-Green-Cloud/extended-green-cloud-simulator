import React from "react"

interface Styles {
    agentContainer: React.CSSProperties,
    badge: React.CSSProperties,
    activeBadge: React.CSSProperties,
    inActiveBadge: React.CSSProperties,
    agentHeader: React.CSSProperties,
    agentNameHeader: React.CSSProperties
}

export const styles: Styles = {
    agentContainer: {
        height: '40%',
        backgroundColor: 'var(--white)',
        marginTop: '20px',
        marginLeft: '20px',
        minWidth: 'fit-content'
    },
    badge: {
        display: 'block',
        textAlign: 'center',
        color: 'white',
        borderRadius: '10px',
        fontSize: '0.8rem',
        fontWeight: '500',
    },
    inActiveBadge: {
        backgroundColor: '#5e5b5b',
    },
    activeBadge: {
        backgroundColor: '#76cd5c',
    },
    agentHeader: {
        color: 'var(--gray-2)',
        fontWeight: '300',
        display: 'flex',
        minWidth: 'fit-content'
    },
    agentNameHeader: {
        marginLeft: '15%',
        fontWeight: '500',
        flexGrow: 1,
        textAlign: 'right',
        paddingBottom: '2px',
        borderBottom: '3px solid var(--green-1)',
    }
}
