import React from 'react'

interface Styles {
   modal: React.CSSProperties
   contentWrapper: React.CSSProperties
   errorWrapper: React.CSSProperties
   errorTextStyle: React.CSSProperties
   statusWrapper: React.CSSProperties
   iconStyle: React.CSSProperties
}

export const styles: Styles = {
   modal: {
      width: '45%'
   },
   contentWrapper: {
      display: 'flex',
      justifyContent: 'space-between',
      flexDirection: 'column',
      height: '100%'
   },
   errorWrapper: {
      marginTop: '20px',
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
   },
   errorTextStyle: {
      marginLeft: '5px',
      color: 'var(--red-1)',
      fontWeight: 500,
      fontSize: 'var(--font-size-3)',
      display: 'flex',
      maxWidth: '40vw',
      wordWrap: 'break-word'
   },
   statusWrapper: {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center',
      marginBottom: '5px',
      fontWeight: 500
   },
   iconStyle: {
      height: '1.5em',
      marginRight: '5px'
   }
}
