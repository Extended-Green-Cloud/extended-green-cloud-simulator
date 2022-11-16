import React from 'react'

interface Styles {
   modalStyle: ReactModal.Styles
   modalTitle: React.CSSProperties
   mainContainer: React.CSSProperties
   contentWrapper: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      overlay: {
         backgroundColor: 'var(--black-transparent)',
      },
      content: {
         margin: 'auto',
         borderRadius: 15,
         display: 'flex',
         flexDirection: 'column',
         overflowY: 'hidden',
      },
   },
   modalTitle: {
      fontSize: 'var(--font-size-8)',
      fontFamily: 'var(--font-1)',
      fontWeight: 300,
      color: 'var(--gray-2)',
      width: '100%',
      borderBottom: 'var(--border-modal)',
      paddingBottom: '5px',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      textAlign: 'center',
   },
   mainContainer: {
      overflowY: 'auto',
      height: 'auto',
      marginTop: '10px',
   },
   contentWrapper: {
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
   },
}
