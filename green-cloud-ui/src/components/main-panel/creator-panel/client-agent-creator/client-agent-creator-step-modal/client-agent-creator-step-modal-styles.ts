import React from 'react'

interface Styles {
   wrapper: React.CSSProperties
   modalContent: React.CSSProperties
   modalContainer: React.CSSProperties
   stepWrapper: React.CSSProperties
   stepTrigger: React.CSSProperties
   stepContent: React.CSSProperties
   stepContentWrapper: React.CSSProperties
   stepContentWrapperHeader: React.CSSProperties
   stepContentWrapperInput: React.CSSProperties
   stepDescriptionStyle: React.CSSProperties
}

export const styles: Styles = {
   modalContent: {
      width: '40%'
   },
   modalContainer: {
      marginTop: 0
   },
   wrapper: {
      display: 'flex',
      justifyContent: 'space-between',
      flexDirection: 'column',
      height: '100%'
   },
   stepWrapper: {
      backgroundColor: 'var(--gray-14)',
      borderLeft: 'var(--border-bold-green)',
      borderRadius: 10,
      marginBottom: '10px'
   },
   stepContent: {
      width: '97%',
      paddingBottom: '10px'
   },
   stepTrigger: {
      fontSize: 'var(--font-size-8)',
      padding: '10px 20px 0px 10px'
   },
   stepContentWrapper: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      width: '100%',
      marginBottom: '10px'
   },
   stepContentWrapperHeader: {
      marginRight: '10px',
      font: 'var(--font-1)',
      fontWeight: 500
   },
   stepContentWrapperInput: {
      width: '50%'
   },
   stepDescriptionStyle: {
      fontSize: 'var(--font-size-6)',
      fontWeight: 400,
      color: 'var(--gray-1)',
      margin: '5px 0px 5px 5px'
   }
}
