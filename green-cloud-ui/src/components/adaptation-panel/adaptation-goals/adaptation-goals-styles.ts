export const test = ''
import React from 'react'

interface Styles {
   containerStyle: React.CSSProperties
   headerStyle: React.CSSProperties
   fieldContainerStyle: React.CSSProperties
   fieldLabelStyle: React.CSSProperties
   fieldValueStyle: React.CSSProperties
}

export const styles: Styles = {
   containerStyle: {
      backgroundColor: 'var(--gray-7)',
      borderLeft: 'var(--border-gray-bold)',
      borderBottomLeftRadius: '8px',
      borderTopLeftRadius: '8px',
      padding: '10px',
      marginBottom: '10px',
   },
   headerStyle: {
      paddingBottom: '5px',
      width: '100%',
      borderBottom: 'var(--border-gray)',
      marginBottom: '15px',
      fontWeight: 400,
      color: 'var(--gray-3)',
   },
   fieldContainerStyle: {
      backgroundColor: 'var(--gray-9)',
      width: undefined,
   },
   fieldLabelStyle: {
      fontSize: 'var(--font-size-4)',
      fontWeight: 400,
   },
   fieldValueStyle: {
      fontWeight: 600,
      fontSize: 'var(--font-size-9)',
   },
}
