import React from 'react'

interface Styles {
   checkBoxContainer: React.CSSProperties
   modalContainer: React.CSSProperties
   modalHeader: React.CSSProperties
}

export const styles: Styles = {
   checkBoxContainer: {
      marginTop: '30px',
      marginBottom: '10px',
   },
   modalContainer: {
      width: '30%',
      height: 'fit-content',
   },
   modalHeader: {
      color: 'var(--white)',
      backgroundColor: 'var(--green-3)',
      border: 'none',
      borderRadius: '100px',
      paddingTop: '5px',
      marginBottom: '10px',
      fontWeight: 500,
   },
}
