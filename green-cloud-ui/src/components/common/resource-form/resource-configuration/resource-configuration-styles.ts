import React from 'react'

interface Styles {
   resourceWrapper: React.CSSProperties
   resourceTrigger: React.CSSProperties
   resourceContent: React.CSSProperties
   resourceFieldWrapper: React.CSSProperties
}

export const styles: Styles = {
   resourceWrapper: {
      backgroundColor: 'var(--gray-14)',
      borderLeft: 'var(--border-bold-green)',
      borderRadius: 10,
      marginBottom: '10px'
   },
   resourceContent: {
      width: '97%',
      paddingBottom: '10px'
   },
   resourceTrigger: {
      fontSize: 'var(--font-size-8)',
      padding: '10px 20px 0px 10px'
   },
   resourceFieldWrapper: {
      borderLeft: 'var(--border-bold-gray-1)',
      borderRadius: 8,
      padding: '5px 10px'
   }
}
