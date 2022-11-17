import React from 'react'

interface Styles {
   agentContainer: React.CSSProperties
}

export const styles: Styles = {
   agentContainer: {
      flexShrink: 0,
      backgroundColor: 'var(--white)',
      height: '50%',
      marginTop: '5%',
      marginLeft: '20px',
      minWidth: 'fit-content',
   },
}
