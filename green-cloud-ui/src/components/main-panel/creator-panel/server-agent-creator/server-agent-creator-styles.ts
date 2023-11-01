import React from 'react'

interface Styles {
   container: React.CSSProperties
   cnaWrapper: React.CSSProperties
   modalContent: React.CSSProperties
   modalContainer: React.CSSProperties
   modalWrapper: React.CSSProperties
}

export const styles: Styles = {
   container: {
      marginTop: '15px'
   },
   cnaWrapper: {
      borderTop: 'var(--border-thin-light-gray)',
      borderBottom: 'var(--border-thin-light-gray)',
      padding: '10px 0',
      marginBottom: '15px'
   },
   modalContent: {
      width: '40%'
   },
   modalContainer: {
      marginTop: 0
   },
   modalWrapper: {
      justifyContent: 'space-between',
      display: 'flex',
      flexDirection: 'column',
      height: '100%'
   }
}
