import React from 'react'

interface Styles {
   collapseStyle: React.CSSProperties
   collapseCloseStyle: React.CSSProperties
   collapseOpenStyle: React.CSSProperties
}

export const styles: Styles = {
   collapseStyle: {
      padding: '5px 10px',
      fontSize: 'var(--font-size-5)',
   },
   collapseCloseStyle: {
      color: 'var(--white)',
      backgroundColor: 'var(--green-1)',
   },
   collapseOpenStyle: {
      color: 'var(--gray-2)',
      backgroundColor: 'var(--gray-4)',
   },
}
