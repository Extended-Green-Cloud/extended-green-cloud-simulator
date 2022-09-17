import React from "react"

interface Styles {
    parentContainer: React.CSSProperties,
    bannerContainer: React.CSSProperties,
    bannerText: React.CSSProperties,
    bannerIcon: React.CSSProperties
}

export const styles: Styles = {
    parentContainer: {
        backgroundColor: 'var(--green-1)',
        boxShadow: '0px 6px 6px -6px rgba(0,0,0,0.4)',
        height: '150px',
    },
    bannerContainer: {
        color: 'var(--white)',
        fontFamily: 'var(--font-1)',
        fontSize: 'var(--font-size-1)',
        fontWeight: '300',
        paddingLeft: '10px',
        display: 'flex',
        alignItems: 'center',
        height: '50px',
        paddingTop: '10px',
    },
    bannerText: {
        paddingLeft: '15px',
        opacity: '0.85',
    },
    bannerIcon: {
        height: '90%',
        opacity: '0.85',
    }
}