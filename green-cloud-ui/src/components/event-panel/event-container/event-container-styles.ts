import React from 'react'

interface Styles {
   singleEventContainer: React.CSSProperties
   triggerWrapper: React.CSSProperties
   triggerContainer: React.CSSProperties
   triggerTitle: React.CSSProperties
   triggerDescription: React.CSSProperties
   collapseWrapper: React.CSSProperties
   contentWrapper: React.CSSProperties
}

export const styles: Styles = {
   singleEventContainer: {
      display: 'flex',
      flexDirection: 'column',
      width: '100%'
   },
   triggerWrapper: {
      paddingRight: '15px'
   },
   triggerContainer: {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      padding: '15px 15px'
   },
   triggerTitle: {
      fontSize: 'var(--font-size-3)',
      fontWeight: 500
   },
   triggerDescription: {
      marginTop: '10px',
      maxWidth: '50vh',
      wordWrap: 'break-word',
      fontSize: 'var(--font-size-6)',
      fontWeight: 400
   },
   collapseWrapper: {
      margin: '15px',
      backgroundColor: 'var(--white)',
      borderRadius: 20,
      paddingBottom: '5px',
      boxShadow: 'var(--card-shadow)'
   },
   contentWrapper: {
      width: '100%'
   }
}
