import React from 'react'

interface Styles {
   badge: React.CSSProperties
   activeBadge: React.CSSProperties
   inActiveBadge: React.CSSProperties
}

export const styles: Styles = {
   badge: {
      display: 'block',
      textAlign: 'center',
      width: 'fit-content',
      color: 'var(--white)',
      fontSize: 'var(--font-size-4)',
      fontWeight: '500',
      borderRadius: '10px',
      padding: '5px 20px'
   },
   inActiveBadge: {
      backgroundColor: 'var(--gray-6)'
   },
   activeBadge: {
      backgroundColor: 'var(--green-2)'
   }
}
