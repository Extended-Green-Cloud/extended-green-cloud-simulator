import React from 'react'

interface Styles {
   modalStyle: ReactModal.Styles
   menuTitle: React.CSSProperties
   buttonWrapper: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      overlay: {
         backgroundColor: 'var(--black-transparent)',
      },
      content: {
         width: '20%',
         margin: 'auto',
         borderRadius: 15,
         display: 'flex',
         flexDirection: 'column',
         height: '50%',
      },
   },
   menuTitle: {
      fontSize: 'var(--font-size-8)',
      fontFamily: 'var(--font-1)',
      fontWeight: 300,
      color: 'var(--gray-2)',
      width: '100%',
      borderBottom: 'var(--border-menu)',
      paddingBottom: '5px',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      textAlign: 'center',
   },
   buttonWrapper: {
      height: '100%',
      marginTop: '20px',
   },
}
