import React from 'react'

interface Styles {
   container: React.CSSProperties
   titleStyle: React.CSSProperties
   fieldWrapper: React.CSSProperties
   fieldLabel: React.CSSProperties
   fieldValue: React.CSSProperties
}

export const styles: Styles = {
   container: {
      padding: '10px',
      marginTop: '10px',
      backgroundColor: 'var(--gray-4)',
      borderLeft: 'var(--border-left-green)',
      borderRadius: '7px',
   },
   titleStyle: {
      fontSize: 'var(--font-size-3)',
      marginBottom: '10px',
      fontWeight: 500,
   },
   fieldWrapper: {
      display: 'flex',
      justifyContent: 'space-between',
      marginBottom: '3px',
   },
   fieldLabel: {
      fontWeight: 400,
      fontSize: 'var(--font-size-5)',
   },
   fieldValue: {
      fontWeight: 500,
      fontSize: 'var(--font-size-3)',
   },
}
