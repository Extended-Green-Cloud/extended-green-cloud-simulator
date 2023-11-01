import React from 'react'

interface Styles {
   wrapper: React.CSSProperties
   content: React.CSSProperties
   selectorWrapper: React.CSSProperties
   buttonWrapper: React.CSSProperties
   creatorHeader: React.CSSProperties
   creatorContent: React.CSSProperties
}

export const styles: Styles = {
   wrapper: {
      height: '100%',
      overflowY: 'hidden',
      display: 'flex',
      flexDirection: 'column'
   },
   content: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      overflowY: 'auto',
      flexGrow: 1
   },
   selectorWrapper: {
      display: 'flex',
      flexDirection: 'column',
      width: '100%',
      overflowY: 'auto'
   },
   buttonWrapper: {
      width: '100%'
   },
   creatorHeader: {
      fontWeight: 500,
      backgroundColor: 'var(--gray-13)',
      padding: '10px',
      borderRadius: 8,
      borderLeft: 'var(--border-semi-bold-green)'
   },
   creatorContent: {
      margin: '0px 20px 20px 20px'
   }
}
