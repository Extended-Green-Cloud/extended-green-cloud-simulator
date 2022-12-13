import React from 'react'

interface Styles {
   modalStyle: React.CSSProperties
   thirdPartyField: React.CSSProperties
   collapseHeader: React.CSSProperties
   collapseContent: React.CSSProperties
   contentRecord: React.CSSProperties
   licenseTable: React.CSSProperties
   headerRecord: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      width: '35%',
      height: '70%',
   },
   thirdPartyField: {
      margin: '15px 0',
   },
   collapseHeader: {
      color: 'var(--gray-3)',
      backgroundColor: 'var(--gray-4)',
      padding: '10px',
      fontWeight: 400,
      marginBottom: 0,
   },
   collapseContent: {
      margin: 'auto',
      width: '90%',
      borderLeft: 'var(--border-gray-bold)',
      backgroundColor: 'var(--gray-4)',
      borderRadius: 10,
   },
   contentRecord: {
      whiteSpace: 'pre-line',
      textAlign: 'justify',
      verticalAlign: 'baseline',
      fontSize: 'var(--font-size-6)',
   },
   licenseTable: {
      borderCollapse: 'separate',
      borderSpacing: '0 15px',
   },
   headerRecord: {
      verticalAlign: 'baseline',
      fontSize: 'var(--font-size-2)',
      fontWeight: 300,
      color: 'var(--gray-1)',
      width: '100px',
   },
}
