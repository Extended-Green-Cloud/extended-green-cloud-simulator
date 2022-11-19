import React from 'react'

interface Styles {
   adaptationContainer: React.CSSProperties
   tabHeader: React.CSSProperties
}

export const styles: Styles = {
   adaptationContainer: {
      flexShrink: 0,
      backgroundColor: 'var(--white)',
      height: '95%',
      marginLeft: '10px',
      minWidth: 'fit-content',
   },
   tabHeader: {
      padding: '5px 15px',
      color: 'var(--gray-2)',
      backgroundColor: 'var(--gray-4)',
      borderLeft: 'var(--border-left-green)',
      fontSize: 'var(--font-size-9)',
      fontWeight: '500',
   },
}
