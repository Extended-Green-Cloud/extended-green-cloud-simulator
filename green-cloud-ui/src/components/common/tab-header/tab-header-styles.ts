import React from 'react'

interface Styles {
   header: React.CSSProperties
   selectedTab: React.CSSProperties
   deselectedTab: React.CSSProperties
   commonTab: React.CSSProperties
}

export const styles: Styles = {
   header: {
      display: 'flex',
      flexWrap: 'wrap',
      justifyContent: 'space-between',
      color: 'var(--gray-2)',
      fontSize: 'var(--font-size-5)',
      fontWeight: '300',
      minWidth: 'fit-content',
      cursor: 'pointer',
   },
   commonTab: {
      textAlign: 'center',
      wordBreak: 'break-all',
      padding: '10px 0',
      width: '50%',
   },
   selectedTab: {
      fontWeight: '400',
      backgroundColor: 'var(--gray-4)',
      borderRadius: 10,
   },
   deselectedTab: {
      color: 'var(--gray-8)',
      fontSize: 'var(--font-size-4)',
   },
}
