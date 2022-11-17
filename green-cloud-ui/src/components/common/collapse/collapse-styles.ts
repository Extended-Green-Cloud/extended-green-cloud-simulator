import React from 'react'

interface Styles {
   collapseStyle: React.CSSProperties
   collapseContentStyle: React.CSSProperties
}

export const styles: Styles = {
   collapseStyle: {
      fontWeight: 400,
      borderRadius: '7px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      cursor: 'pointer',
      marginBottom: '10px',
   },
   collapseContentStyle: {
      width: '90%',
      margin: 'auto',
      marginBottom: '10px',
   },
}
