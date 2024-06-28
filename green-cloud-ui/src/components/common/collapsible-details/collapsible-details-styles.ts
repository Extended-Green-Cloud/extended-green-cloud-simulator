import React from 'react'

interface Styles {
   mainFieldWrapper: React.CSSProperties
   dropdownWrapper: React.CSSProperties
   dropdownContent: React.CSSProperties
   dropdownTrigger: React.CSSProperties
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
   }
}
