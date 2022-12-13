import React from 'react'

interface Styles {
   modalStyle: React.CSSProperties
   valueStyle: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      width: '35%',
      height: '50%',
   },
   valueStyle: {
      fontSize: 'var(--font-size-2)',
      fontWeight: 500,
      padding: '5px 10px',
   },
}
