import React from 'react'

interface Styles {
   characteristicCommon: React.CSSProperties
   textStyle: React.CSSProperties
   contentWrapper: React.CSSProperties
}

export const styles: Styles = {
   characteristicCommon: {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      padding: '10px 10px 15px 10px',
      marginTop: '10px',
      backgroundColor: 'var(--gray-9)',
      borderRadius: 8
   },
   textStyle: {
      fontWeight: 500,
      fontSize: 'var(--font-size-3)',
      fontFamily: 'var(--font-1)',
      width: '45%'
   },
   contentWrapper: {
      width: '50%'
   }
}
