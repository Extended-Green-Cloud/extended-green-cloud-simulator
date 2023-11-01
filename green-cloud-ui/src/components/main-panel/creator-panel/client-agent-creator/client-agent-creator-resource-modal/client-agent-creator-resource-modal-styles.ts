import React from 'react'

interface Styles {
   modalContent: React.CSSProperties
   modalContainer: React.CSSProperties
   wrapper: React.CSSProperties
}

export const styles: Styles = {
   wrapper: {
      justifyContent: 'space-between',
      display: 'flex',
      flexDirection: 'column',
      height: '100%'
   },
   modalContent: {
      width: '40%'
   },
   modalContainer: {
      marginTop: 0
   }
}
