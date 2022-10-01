import React from "react"

interface Styles {
    agentContainer: React.CSSProperties,
    agentHeader: React.CSSProperties,
    agentNameHeader: React.CSSProperties
}

export const styles: Styles = {
    agentContainer: {
        flexShrink: 0,
        backgroundColor: 'var(--white)',
        height: '40%',
        marginTop: '5%',
        marginLeft: '20px',
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
