import React from "react"

interface Styles {
    detailsContainer: React.CSSProperties,
    label: React.CSSProperties,
    value: React.CSSProperties
}

export const styles: Styles = {
    detailsContainer: {
        display: 'flex',
        width: '100%',
        alignItems: 'center',
        marginBottom: '10px',
        paddingTop: '5px',
        paddingBottom: '5px',
        backgroundColor: 'var(--gray-4)',
        borderLeft: '6px solid var(--gray-2)',
    },
    label: {
        width: '70%',
        textAlign: 'left',
        paddingLeft: '10px',
        fontWeight: '300',
        fontSize: 'var(--font-size-2)'
    },
    value: {
        width: '30%',
        textAlign: 'right',
        paddingRight: '10px',
        fontSize: 'var(--font-size-3)'
    }
}