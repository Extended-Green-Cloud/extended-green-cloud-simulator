import React from 'react'

interface Styles {
   chartWrapper: React.CSSProperties
   chartContainerWrapper: React.CSSProperties
}

export const styles: Styles = {
   chartContainerWrapper: {
      padding: '10px',
      display: 'grid',
      gridTemplateColumns: 'repeat(2, 1fr)',
      rowGap: '20px',
      columnGap: '10px'
   },
   chartWrapper: {
      height: '17vw',
      width: '100%',
      borderRadius: 20,
      minHeight: 0,
      minWidth: '0'
   }
}
