import React from 'react'

interface Styles {
   wrapper: React.CSSProperties
   indicatorWrapper: React.CSSProperties
}

export const styles: Styles = {
   wrapper: {
      display: 'flex',
      flexDirection: 'row',
      width: '100%',
      marginTop: '10px',
      justifyContent: 'center',
      height: '100%'
   },
   indicatorWrapper: {
      margin: '0px 2px',
      width: '9.8vw',
      height: '90%'
   }
}
