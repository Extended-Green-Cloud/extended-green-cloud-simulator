import React from 'react'

interface Styles {
   mainFieldWrapper: React.CSSProperties
   dropdownWrapper: React.CSSProperties
   dropdownContent: React.CSSProperties
   dropdownTrigger: React.CSSProperties
   subEntryWrapper: React.CSSProperties
}

export const styles: Styles = {
   mainFieldWrapper: {
      marginBottom: '10px'
   },
   dropdownWrapper: {
      backgroundColor: 'var(--gray-14)',
      borderRadius: 10
   },
   dropdownContent: {
      width: '95%'
   },
   dropdownTrigger: {
      fontSize: 'var(--font-size-2)',
      borderRadius: 10,
      padding: '5px 20px 0px 20px'
   },
   subEntryWrapper: {
      fontSize: 'var(--font-size-5)',
      fontWeight: 500,
      borderBottom: 'var(--border-thin-gray)',
      paddingBottom: '5px',
      width: '50%',
      marginBottom: '10px'
   }
}
