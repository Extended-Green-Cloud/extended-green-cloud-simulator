import React from 'react'

interface Styles {
   clientStatistics: React.CSSProperties
   clientContent: React.CSSProperties
}

export const styles: Styles = {
   clientContent: {
      height: '100%',
      display: 'flex',
      overflowY: 'scroll',
      flexDirection: 'column',
   },
   clientStatistics: {
      marginTop: '10px',
      overflowX: 'hidden',
      overflowY: 'scroll',
      height: '100%',
   },
}
