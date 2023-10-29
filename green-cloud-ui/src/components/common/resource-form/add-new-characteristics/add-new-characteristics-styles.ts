import React from 'react'

interface Styles {
   newResourceWrapper: React.CSSProperties
   newResourceText: React.CSSProperties
   newResourceButton: React.CSSProperties
}

export const styles: Styles = {
   newResourceWrapper: {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      width: '100%',
      marginBottom: '20px'
   },
   newResourceText: {
      width: '50%'
   },
   newResourceButton: {
      marginLeft: '30px',
      width: '50%'
   }
}
