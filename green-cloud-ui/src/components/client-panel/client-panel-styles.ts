import React from "react"
import { StylesConfig, ThemeConfig, Theme } from 'react-select';
import { AgentOption } from "./client-panel-config";

interface Styles {
    clientContainer: React.CSSProperties,
    clientStatistics: React.CSSProperties,
    clientContent: React.CSSProperties,
    select: StylesConfig<AgentOption>,
    selectTheme: ThemeConfig
}

export const styles: Styles = {
    clientContainer: {
        flexShrink: 0,
        marginBottom: '5%',
        backgroundColor: 'var(--white)',
        minWidth: 'fit-content',
        height: '45%'
    },
    clientContent: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column'
    },
    clientStatistics: {
        marginTop: '10px',
        overflowX: 'hidden',
        msOverflowY: 'scroll',
        height: '100%'
    },
    select: {
        container: (styles: any) => ({
            ...styles,
            paddingBottom: '10px'
        }),
        placeholder: (styles: any) => ({
            ...styles,
            fontSize: 'var(--font-size-2)',
            fontFamily: 'var(--font-1)',
            textTransform: 'uppercase'
        }),
        groupHeading: (styles: any) => ({
            ...styles,
            color: 'var(--green-1)',
            opacity: 0.8,
            fontFamily: 'var(--font-1)',
            fontSize: 'var(--font-size-7)',
            fontWeight: 400,
            marginLeft: '5px',
            width: '50%',
            borderBottom: '1px solid var(--green-1)'
        }),
        noOptionsMessage: (styles: any) => ({
            ...styles,
            color: 'var(--gray2)'
        }),
        menu: (styles: any) => ({
            ...styles,
            marginTop: '-8px'
        })
    },
    selectTheme: (theme: Theme) => {
        return ({
            ...theme,
            borderRadius: 0,
            colors: {
                ...theme.colors,
                text: 'orangered',
                primary50: 'var(--gray-5)',
                primary25: 'var(--white)',
                primary: 'var(--green-4)',
            }
        })
    }
}