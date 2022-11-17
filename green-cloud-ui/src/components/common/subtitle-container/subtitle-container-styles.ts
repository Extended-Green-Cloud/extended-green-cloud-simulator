import React from 'react'

interface Styles {
   descriptionContainer: React.CSSProperties
   descriptionText: React.CSSProperties
}

export const styles: Styles = {
   descriptionContainer: {
      width: '70%',
      paddingTop: '15px',
      paddingBottom: '10px',
      display: 'flex',
      alignItems: 'center',
   },
   descriptionText: {
      fontSize: 'var(--font-size-6)',
      marginLeft: '7px',
   },
}
