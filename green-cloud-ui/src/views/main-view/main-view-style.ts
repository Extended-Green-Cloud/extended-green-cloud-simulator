import React from 'react'

interface Styles {
   mainContainer: React.CSSProperties
   contentContainer: React.CSSProperties
   leftContentContainer: React.CSSProperties
   rightContentContainer: React.CSSProperties
}

export const styles: Styles = {
   mainContainer: {
      backgroundColor: 'var(--beige-1)',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      marginBottom: '5px',
   },
   contentContainer: {
      display: 'flex',
      height: '75%',
      minHeight: 0,
      marginTop: '-7%',
      minWidth: 'fit-content',
   },
   leftContentContainer: {
      width: '10%',
      flexGrow: 1,
      display: 'flex',
      height: '100%',
      flexDirection: 'column',
      minWidth: '0',
      marginLeft: '20px',
   },
   rightContentContainer: {
      width: '10%',
      flexGrow: 1,
      display: 'flex',
      height: '100%',
      flexDirection: 'column',
      minWidth: '0',
      marginRight: '20px',
   },
}
