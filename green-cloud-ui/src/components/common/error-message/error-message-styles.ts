import React from 'react'

interface Styles {
   errorWrapper: React.CSSProperties
   errorTextStyle: React.CSSProperties
}

export const styles: Styles = {
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
   }
}
