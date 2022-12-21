import React from 'react'

interface Styles {
   fieldContainerStyle: React.CSSProperties
   fieldValueStyle: React.CSSProperties
   fieldLabelStyle: React.CSSProperties
   iconStyle: React.CSSProperties
   goalWrapper: React.CSSProperties
   qualityText: React.CSSProperties
   tooltipContent: React.CSSProperties
}

export const styles: Styles = {
   fieldContainerStyle: {
      paddingTop: '10px',
      paddingBottom: '10px',
      backgroundColor: 'var(--gray-7)',
   },
   fieldValueStyle: {
      paddingRight: '15px',
      fontSize: 'var(--font-size-3)',
      fontWeight: 600,
   },
   fieldLabelStyle: {
      paddingLeft: '15px',
      fontSize: 'var(--font-size-4)',
   },
   iconStyle: {
      height: '25px',
   },
   goalWrapper: {
      display: 'flex',
      flexDirection: 'row',
      justifyContent: 'flex-end',
      alignItems: 'flex-end',
   },
   qualityText: {
      marginLeft: '5px',
      fontSize: 'var(--font-size-4)',
      color: 'var(--green-1)',
      width: '40px',
      minWidth: 'fit-content',
   },
   tooltipContent: {
      marginTop: '2px',
      display: 'flex',
      flexDirection: 'column',
      marginLeft: '25px',
      alignItems: 'flex-start',
   },
}
