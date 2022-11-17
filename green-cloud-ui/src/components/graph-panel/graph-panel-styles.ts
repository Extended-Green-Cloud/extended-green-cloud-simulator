import React from 'react'

interface Styles {
   graphContainer: React.CSSProperties
   graphContent: React.CSSProperties
}

export const styles: Styles = {
   graphContainer: {
      flexGrow: 1,
      height: '105%',
      backgroundColor: 'var(--white)',
      minWidth: 'min-content',
      marginTop: '-2.5%',
      marginLeft: '20px',
      marginRight: '20px',
   },
   graphContent: {
      borderTop: 'var(--border-gray)',
   },
}
