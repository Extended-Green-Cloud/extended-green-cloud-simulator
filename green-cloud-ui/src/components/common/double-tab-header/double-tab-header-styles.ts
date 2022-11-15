import React from 'react'

interface Styles {
   header: React.CSSProperties
   selectedTab: React.CSSProperties
   deselectedTab: React.CSSProperties
   secondTabStyle: React.CSSProperties
}

export const styles: Styles = {
   header: {
      display: 'flex',
      justifyContent: 'space-between',
      marginRight: '25px',
      color: 'var(--gray-2)',
      fontWeight: '300',
      minWidth: 'fit-content',
      cursor: 'pointer',
   },
   selectedTab: {
      borderBottom: '1px solid var(--gray-1)',
      paddingBottom: '10px',
      width: '50%',
   },
   deselectedTab: {
      color: 'var(--gray-8)',
      width: '50%',
   },
   secondTabStyle: {
      textAlign: 'right',
   },
}
