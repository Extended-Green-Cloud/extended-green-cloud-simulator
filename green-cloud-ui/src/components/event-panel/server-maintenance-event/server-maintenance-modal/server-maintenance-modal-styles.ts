import React from 'react'

interface Styles {
   modal: React.CSSProperties
   modalContainer: React.CSSProperties
   contentWrapper: React.CSSProperties
   statusWrapper: React.CSSProperties
   iconStyle: React.CSSProperties
}

export const styles: Styles = {
   modal: {
      width: '45%'
   },
   modalContainer: {
      marginTop: 0
   },
   contentWrapper: {
      display: 'flex',
      justifyContent: 'space-between',
      flexDirection: 'column',
      height: '100%'
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
