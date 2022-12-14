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
      marginLeft: '10px',
      marginRight: '10px',
   },
   graphContent: {
      borderTop: 'var(--border-gray)',
   },
}
