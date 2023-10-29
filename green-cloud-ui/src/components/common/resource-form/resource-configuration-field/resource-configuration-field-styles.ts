import React from 'react'

interface Styles {
   fieldWrapper: React.CSSProperties
   fieldTrigger: React.CSSProperties
   fieldContent: React.CSSProperties
}

export const styles: Styles = {
   fieldWrapper: {
      borderRadius: 10,
      marginTop: '5px',
      marginBottom: '5px'
   },
   fieldTrigger: {
      fontSize: 'var(--font-size-3)',
      borderRadius: 0,
      borderBottom: 'var(--border-thin-gray)',
      padding: '5px 10px 0px 10px'
   },
   fieldContent: {
      width: '98%'
   }
}
