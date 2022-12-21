import React from 'react'

interface Styles {
   tooltipStyle: React.CSSProperties
   tooltipHeader: React.CSSProperties
   tooltipHeaderText: React.CSSProperties
}

export const styles: Styles = {
   tooltipStyle: {
      backgroundColor: 'var(--white)',
      boxShadow: 'var(--card-shadow)',
      opacity: 0.98,
      color: 'var(--gray-3)',
      fontWeight: 400,
      fontFamily: 'var(--font-1)',
      fontSize: 'var(--font-size-2)',
      borderRadius: 7,
   },
   tooltipHeader: {
      display: 'flex',
      alignItems: 'center',
   },
   tooltipHeaderText: {
      marginLeft: '5px',
   },
}
