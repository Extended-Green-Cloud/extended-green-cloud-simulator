import React from 'react'
import { IconProps } from 'types/assets/icon-props'

interface Styles {
   collapseStyle: React.CSSProperties
   collapseContentStyle: React.CSSProperties
   triggerIcon: IconProps
}

export const styles: Styles = {
   collapseStyle: {
      fontWeight: 400,
      borderRadius: '7px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      cursor: 'pointer',
      marginBottom: '8px'
   },
   collapseContentStyle: {
      width: '90%',
      margin: 'auto',
      marginBottom: '10px'
   },
   triggerIcon: {
      size: '20px',
      color: 'var(--gray-3)'
   }
}
