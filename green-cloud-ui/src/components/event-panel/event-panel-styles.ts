import React from 'react'

interface Styles {
   eventContainer: React.CSSProperties
   singleEventContainer: React.CSSProperties
   singleEventParentContainer: React.CSSProperties
}

export const styles: Styles = {
   eventContainer: {
      backgroundColor: 'var(--white)',
      minWidth: 'fit-content',
      minHeight: '40%',
   },
   singleEventParentContainer: {
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
   },
   singleEventContainer: {
      display: 'flex',
      minHeight: 'fit-content',
      flexGrow: 0.5,
      justifyContent: 'space-between',
      flexDirection: 'column',
      padding: '10px',
      margin: '10px',
      borderLeft: 'var(--border-event)',
      boxShadow: 'var(--event-shadow)',
      borderTopLeftRadius: 7,
      borderBottomLeftRadius: 7,
   },
}
