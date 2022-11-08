import React from 'react'
import { StylesConfig, ThemeConfig, Theme } from 'react-select'
import { AgentOption } from './client-panel-config'

interface Styles {
   clientContainer: React.CSSProperties
   clientStatistics: React.CSSProperties
   clientContent: React.CSSProperties
   checkBox: React.CSSProperties
   checkContainer: React.CSSProperties
   select: StylesConfig<AgentOption>
   selectTheme: ThemeConfig
   collapse: React.CSSProperties
}

export const styles: Styles = {
   clientContainer: {
      flexShrink: 0,
      marginBottom: '5%',
      backgroundColor: 'var(--white)',
      minWidth: 'fit-content',
      height: '45%',
   },
   clientContent: {
      height: '100%',
      display: 'flex',
      overflowY: 'scroll',
      flexDirection: 'column',
   },
   clientStatistics: {
      marginTop: '10px',
      overflowX: 'hidden',
      msOverflowY: 'scroll',
      height: '100%',
   },
   checkBox: {
      width: '100px',
      fontSize: 'var(--font-size-6)',
      fontFamily: 'var(--font-1)',
      marginRight: '5px',
   },
   checkContainer: {
      marginBottom: '10px',
      display: 'table',
      marginLeft: '5px',
   },
   collapse: {
      display: 'flex',
      alignItems: 'center',
      flexDirection: 'row',
      justifyContent: 'space-between',
      fontSize: 'var(--font-size-4)',
      marginBottom: '5px',
      backgroundColor: 'var(--green-3)',
      padding: '2px 5px',
      color: 'var(--white)',
      borderRadius: 4,
   },
   select: {
      container: (styles: any) => ({
         ...styles,
         paddingBottom: '10px',
      }),
      control: (styles: any) => ({
         ...styles,
         width: '99.5%',
         margin: 'auto',
      }),
      placeholder: (styles: any) => ({
         ...styles,
         fontSize: 'var(--font-size-2)',
         fontFamily: 'var(--font-1)',
         textTransform: 'uppercase',
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
         borderBottom: 'var(--border-client-list)',
      }),
      noOptionsMessage: (styles: any) => ({
         ...styles,
         color: 'var(--gray2)',
      }),
      menu: (styles: any) => ({
         ...styles,
         marginTop: '2px',
         border: '1px solid var(--gray-5)',
      }),
   },
   selectTheme: (theme: Theme) => {
      return {
         ...theme,
         borderRadius: 7,
         colors: {
            ...theme.colors,
            text: 'orangered',
            primary50: 'var(--gray-5)',
            primary25: 'var(--white)',
            primary: 'var(--green-4)',
         },
      }
   },
}
