import React from 'react'

interface Styles {
   singleEventContainer: React.CSSProperties
   collapseTriggerWrapper: React.CSSProperties
   collapseWrapper: React.CSSProperties
   contentWrapper: React.CSSProperties
}

export const styles: Styles = {
   singleEventContainer: {
      display: 'flex',
      flexDirection: 'column',
   },
   collapseTriggerWrapper: {
      backgroundColor: 'var(--gray-2)',
      color: 'var(--white)',
      padding: '10px 15px',
      borderRadius: '8px 8px 0 0px',
   },
   collapseWrapper: {
      marginLeft: '15px',
      backgroundColor: 'var(--gray-7)',
      paddingBottom: '5px',
      borderBottomLeftRadius: '8px',
      borderBottomRightRadius: '8px',
   },
   contentWrapper: {
      padding: '0 20px',
   },
}
