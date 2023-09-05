import React from 'react'

interface Styles {
   modalStyle: React.CSSProperties
   modalHeader: React.CSSProperties
   wrapper: React.CSSProperties
   chartContentWrapper: React.CSSProperties
   chartWrapper: React.CSSProperties
   chartContainerWrapper: React.CSSProperties
   avgContentWrapper: React.CSSProperties
   avgContainerWrapper: React.CSSProperties
   headerContainer: React.CSSProperties
   headerText: React.CSSProperties
}

export const styles: Styles = {
   modalStyle: {
      width: '80%',
      height: '85%',
      paddingTop: '10px',
      overflowX: 'hidden'
   },
   modalHeader: {
      border: 'none',
      fontWeight: 600,
      paddingLeft: '10px',
      paddingTop: '10px',
      color: 'var(--gray-3)',
      fontSize: 'var(--font-size-11)'
   },
   headerText: {
      padding: '10px 20px'
   },
   headerContainer: {
      borderTopLeftRadius: '20px',
      borderTopRightRadius: '20px',
      width: '100%',
      color: 'var(--white)',
      fontSize: 'var(--font-size-4)',
      fontWeight: 500,
      backgroundColor: 'var(--gray-12)'
   },
   wrapper: {
      height: '97%',
      width: '100%',
      display: 'flex',
      flexDirection: 'row',
      minWidth: 'fit-content'
   },
   chartContentWrapper: {
      height: '100%',
      borderRadius: '20px',
      boxShadow: 'var(--card-shadow)',
      minHeight: 0,
      margin: '10px',
      width: '70%',
      display: 'flex',
      flexDirection: 'column',
      minWidth: '0'
   },
   chartContainerWrapper: {
      padding: '20px 20px 30px 20px',
      display: 'grid',
      gridTemplateColumns: 'repeat(2, 1fr)',
      rowGap: '25px',
      columnGap: '15px',
      overflowY: 'scroll',
      overflowX: 'hidden'
   },
   avgContentWrapper: {
      height: '100%',
      borderRadius: '20px',
      boxShadow: 'var(--card-shadow)',
      minHeight: 0,
      minWidth: 'fit-content',
      margin: '10px',
      width: '30%',
      display: 'flex',
      flexDirection: 'column'
   },
   avgContainerWrapper: {
      padding: '10px 0px',
      overflowY: 'auto',
      overflowX: 'hidden',
      display: 'flex',
      width: '100%',
      flexGrow: 1,
      flexDirection: 'column',
      alignItems: 'center'
   },
   chartWrapper: {
      height: '17vw',
      width: '100%',
      borderRadius: 20,
      minHeight: 0,
      minWidth: '0'
   }
}
