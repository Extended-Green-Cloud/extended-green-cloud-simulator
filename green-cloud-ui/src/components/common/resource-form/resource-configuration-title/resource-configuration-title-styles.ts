import React from 'react'

interface Styles {
   wrapper: React.CSSProperties
   text: React.CSSProperties
}

export const styles: Styles = {
   wrapper: {
      display: 'flex',
      flexDirection: 'row',
      alignContent: 'flex-end',
      justifyContent: 'space-between',
      marginRight: '10px'
   },
   text: {
      marginLeft: '5px'
   }
}
