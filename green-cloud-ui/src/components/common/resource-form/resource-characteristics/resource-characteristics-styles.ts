import React from 'react'

interface Styles {
   characteristicFieldWrapper: React.CSSProperties
   characteristicFieldTrigger: React.CSSProperties
}

export const styles: Styles = {
   characteristicFieldWrapper: {
      backgroundColor: 'var(--gray-5)',
      borderRadius: 10,
      marginTop: '5px',
      marginBottom: '5px'
   },
   characteristicFieldTrigger: {
      fontSize: 'var(--font-size-3)',
      padding: '10px 10px 0px 10px'
   }
}
