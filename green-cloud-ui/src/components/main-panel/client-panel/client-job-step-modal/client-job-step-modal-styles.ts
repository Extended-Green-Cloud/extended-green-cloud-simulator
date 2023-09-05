import React from 'react'

interface Styles {
   modalStyle: React.CSSProperties
   valueStyle: React.CSSProperties
   stepWrapper: React.CSSProperties
   stepValueContainer: React.CSSProperties
   stepValueLabel: React.CSSProperties
   stepValue: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      width: '35%',
      height: '50%'
   },
   valueStyle: {
      fontSize: 'var(--font-size-2)',
      fontWeight: 500,
      padding: '5px 20px'
   },
   stepWrapper: {
      width: '50%'
   },
   stepValueContainer: {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
   },
   stepValueLabel: {
      width: '50%',
      textAlign: 'left',
      fill: 'var(--gray-3)',
      fontWeight: 600,
      fontSize: 'var(--font-size-7)'
   },
   stepValue: {
      width: '50%',
      textAlign: 'right',
      fontWeight: 500,
      fontSize: 'var(--font-size-4)'
   }
}
