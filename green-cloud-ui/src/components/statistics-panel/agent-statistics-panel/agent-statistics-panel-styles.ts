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
        flexShrink: 0,
        backgroundColor: 'var(--white)',
        height: '45%',
        marginTop: '5%',
        marginLeft: '20px',
    },
    badge: {
        display: 'block',
        textAlign: 'center',
        color: 'var(--white)',
        borderRadius: '10px',
        fontSize: '0.8rem',
        fontWeight: '500',
    },
    inActiveBadge: {
        backgroundColor: 'var(--gray-6)',
    },
    activeBadge: {
        backgroundColor: 'var(--green-4)',
    },
    agentHeader: {
        display: 'flex',
        color: 'var(--gray-2)',
        fontWeight: '300',
        minWidth: 'fit-content',
    },
    agentNameHeader: {
        flexGrow: 1,
        textAlign: 'right',
        marginLeft: '15%',
        fontWeight: '500',
        paddingBottom: '2px',
        borderBottom: 'var(--border-agent-panel)',
        minWidth: 0
    }
}
