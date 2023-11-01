import React from 'react'

interface Styles {
   wrapper: React.CSSProperties
   textInput: React.CSSProperties
   button: React.CSSProperties
}

export const styles: Styles = {
   wrapper: {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      width: '100%',
      marginBottom: '20px'
   },
   textInput: {
      width: '50%'
   },
   button: {
      marginLeft: '30px',
      width: '50%'
   }
}
