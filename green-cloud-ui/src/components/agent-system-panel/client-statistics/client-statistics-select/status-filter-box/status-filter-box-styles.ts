import React from 'react'

interface Styles {
   checkBox: React.CSSProperties
   checkContainer: React.CSSProperties
   collapse: React.CSSProperties
}

export const styles: Styles = {
   checkBox: {
      width: '100px',
      fontSize: 'var(--font-size-6)',
      fontFamily: 'var(--font-1)',
      marginRight: '5px',
   },
   checkContainer: {
      marginBottom: '10px',
      display: 'table',
      marginLeft: '5px',
   },
   collapse: {
      fontSize: 'var(--font-size-4)',
      backgroundColor: 'var(--green-3)',
      padding: '3px 7px',
      color: 'var(--white)',
   },
}
