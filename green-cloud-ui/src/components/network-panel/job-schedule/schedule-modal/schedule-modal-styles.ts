import React from 'react'

interface Styles {
   modalStyle: React.CSSProperties
   jobContainer: React.CSSProperties
   jobHeader: React.CSSProperties
   jobField: React.CSSProperties
   jobFieldLabel: React.CSSProperties
   jobFieldVal: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      width: '30%',
      height: '80%',
   },
   jobContainer: {
      padding: '10px',
      marginTop: '10px',
      backgroundColor: 'var(--gray-4)',
      borderLeft: 'var(--border-left-green)',
      borderRadius: '7px',
   },
   jobHeader: {
      fontSize: 'var(--font-size-3)',
      marginBottom: '10px',
      fontWeight: 500,
   },
   jobField: {
      display: 'flex',
      justifyContent: 'space-between',
      marginBottom: '3px',
   },
   jobFieldLabel: {
      fontWeight: 400,
      fontSize: 'var(--font-size-5)',
   },
   jobFieldVal: {
      fontWeight: 500,
      fontSize: 'var(--font-size-3)',
   },
}
