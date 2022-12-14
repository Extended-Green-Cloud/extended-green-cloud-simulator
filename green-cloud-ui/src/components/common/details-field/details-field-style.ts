import React from 'react'

interface Styles {
   detailsContainer: React.CSSProperties
   headerLabel: React.CSSProperties
   headerContainer: React.CSSProperties
   label: React.CSSProperties
   value: React.CSSProperties
   valueText: React.CSSProperties
}

export const styles: Styles = {
   detailsContainer: {
      display: 'flex',
      alignItems: 'center',
      width: '100%',
      marginBottom: '10px',
      paddingTop: '5px',
      paddingBottom: '5px',
      backgroundColor: 'var(--gray-4)',
      borderLeft: 'var(--border-detail)',
      borderRadius: 7,
   },
   headerLabel: {
      fontSize: 'var(--font-size-8)',
      fontWeight: '400',
   },
   headerContainer: {
      borderLeft: 'var(--border-left-green)',
   },
   label: {
      textAlign: 'left',
      width: '50%',
      paddingLeft: '10px',
      fontWeight: '300',
      fontSize: 'var(--font-size-2)',
   },
   value: {
      display: 'flex',
      justifyContent: 'flex-end',
      marginRight: '5px',
      width: '50%',
      textAlign: 'right',
      paddingRight: '10px',
   },
   valueText: {
      fontWeight: '400',
      fontSize: 'var(--font-size-3)',
   },
}
