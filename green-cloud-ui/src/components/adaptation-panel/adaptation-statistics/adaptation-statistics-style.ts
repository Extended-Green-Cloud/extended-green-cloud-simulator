import React from 'react'

interface Styles {
   fieldContainerStyle: React.CSSProperties
   fieldValueStyle: React.CSSProperties
   fieldLabelStyle: React.CSSProperties
}

export const styles: Styles = {
   fieldContainerStyle: {
      paddingTop: '10px',
      paddingBottom: '10px',
      backgroundColor: 'var(--gray-7)',
   },
   fieldValueStyle: {
      paddingRight: '15px',
      fontSize: 'var(--font-size-3)',
      fontWeight: 600,
   },
   fieldLabelStyle: {
      paddingLeft: '15px',
      fontSize: 'var(--font-size-4)',
   },
}
