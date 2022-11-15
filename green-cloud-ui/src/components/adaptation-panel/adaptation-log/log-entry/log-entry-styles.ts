import React from 'react'

interface Styles {
   wrapperContainer: React.CSSProperties
   iconContainer: React.CSSProperties
   iconSize: React.DetailedHTMLProps<
      React.ImgHTMLAttributes<HTMLImageElement>,
      HTMLImageElement
   >
   contentContainer: React.CSSProperties
   descriptionContainer: React.CSSProperties
   headerContainer: React.CSSProperties
   agentName: React.CSSProperties
}

export const styles: Styles = {
   wrapperContainer: {
      display: 'flex',
      marginBottom: '10px',
   },
   iconContainer: {
      backgroundColor: 'var(--gray-2)',
      padding: '5px 10px',
      justifyContent: 'center',
      display: 'flex',
      alignItems: 'center',
      borderTopLeftRadius: '15px',
      borderBottomLeftRadius: '15px',
   },
   iconSize: {
      width: '30',
      height: '30',
   },
   contentContainer: {
      backgroundColor: 'var(--gray-7)',
      padding: '10px',
      display: 'flex',
      width: '100%',
      flexDirection: 'column',
   },
   descriptionContainer: {
      margin: '5px 0px',
      fontSize: 'var(--font-size-4)',
      fontWeight: 300,
   },
   headerContainer: {
      fontWeight: 400,
      fontSize: 'var(--font-size-5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: '5px',
   },
   agentName: {
      fontSize: 'var(--font-size-9)',
      paddingBottom: '5px',
      fontWeight: 300,
      textAlign: 'right',
      color: 'var(--gray-3)',
      borderBottom: '1px solid var(--gray-1)',
   },
}
