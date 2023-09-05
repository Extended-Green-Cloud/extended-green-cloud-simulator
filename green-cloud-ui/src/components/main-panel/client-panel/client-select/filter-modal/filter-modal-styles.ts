import React from 'react'

interface Styles {
   modalContainer: React.CSSProperties
   modalHeader: React.CSSProperties
}

export const styles: Styles = {
   modalContainer: {
      width: '40%',
      minWidth: '0',
      height: 'fit-content'
   },
   modalHeader: {
      color: 'var(--gray-3)',
      border: 'none',
      borderRadius: '100px',
      paddingTop: '5px',
      marginBottom: '10px',
      fontWeight: 500
   }
}
