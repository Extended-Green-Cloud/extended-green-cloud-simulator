import React from 'react'

interface Styles {
   fieldWrapper: React.CSSProperties
   fieldHeader: React.CSSProperties
}

export const styles: Styles = {
   fieldWrapper: {
      marginTop: '15px',
      marginBottom: '20px',
   },
   fieldHeader: {
      marginBottom: '10px',
      fontSize: 'var(--font-size-3)',
      padding: '5px 15px',
      borderBottom: 'var(--border-gray)',
      fontWeight: 400,
      width: '50%',
   },
}
