import React from "react"

interface Styles {
    badge: React.CSSProperties,
    activeBadge: React.CSSProperties,
    inActiveBadge: React.CSSProperties
}

export const styles: Styles = {

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
    }
}
