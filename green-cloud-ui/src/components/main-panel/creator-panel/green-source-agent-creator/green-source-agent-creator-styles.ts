import React from 'react'

interface Styles {
   container: React.CSSProperties
   serverWrapper: React.CSSProperties
}

export const styles: Styles = {
   container: {
      marginTop: '15px'
   },
   serverWrapper: {
      borderTop: 'var(--border-thin-light-gray)',
      borderBottom: 'var(--border-thin-light-gray)',
      padding: '10px 0',
      marginBottom: '15px'
   }
}
