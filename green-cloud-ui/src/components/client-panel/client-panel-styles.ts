import { Agent } from "@types";
import React from "react"
import { StylesConfig, ThemeConfig, Theme } from 'react-select';

interface Styles {
    clientContainer: React.CSSProperties,
    clientStatistics: React.CSSProperties,
    clientContent: React.CSSProperties,
    select: StylesConfig<AgentOption>,
    selectTheme: ThemeConfig
}

export interface AgentOption {
    value: Agent,
    label: string
}

export const styles: Styles = {
    clientContainer: {
        backgroundColor: 'var(--white)',
        flexGrow: 1,
        flexShrink: 0,
        marginTop: '-5%',
        marginBottom: '20px',
        minWidth: 'fit-content'
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
        noOptionsMessage: (styles: any) => ({
            ...styles,
            color: 'var(--gray2)'
        }),
        menu: (styles: any) => ({
            ...styles,
            marginTop: '-8px'
        })
    },
    selectTheme: (theme: Theme) => {return({
        ...theme,
      borderRadius: 0,
      colors: {
      ...theme.colors,
        text: 'orangered',
        primary50: 'var(--gray-5)',
        primary25: 'var(--gray-4)',
        primary: 'var(--green-4)',
      }
    })}
}