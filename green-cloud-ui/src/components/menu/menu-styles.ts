import React from 'react'

interface Styles {
   parentContainer: React.CSSProperties
   logoContainer: React.CSSProperties
   menuContainer: React.CSSProperties
   menuContent: React.CSSProperties
   menuHeader: React.CSSProperties
   menuCloudIcon: React.CSSProperties
   menuModalIcon: React.CSSProperties
}

export const styles: Styles = {
   parentContainer: {
      backgroundColor: 'var(--green-1)',
      boxShadow: 'var(--banner-shadow)',
      height: '100%',
      width: '130px'
   },
   menuContainer: {
      marginTop: '50px',
      display: 'flex',
      flexDirection: 'column'
   },
   menuContent: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'space-between',
      height: '100%'
   },
   menuModalIcon: {
      height: '55px',
      opacity: 0.95,
      marginBottom: '20px'
   },
   logoContainer: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      color: 'var(--white)',
      fontFamily: 'var(--font-1)',
      fontSize: 'var(--font-size-1)',
      fontWeight: '300',
      paddingLeft: '5px',
      paddingRight: '5px',
      paddingTop: '25px',
      height: '50px'
   },
   menuHeader: {
      paddingTop: '15px',
      opacity: '0.85',
      width: '70%',
      fontSize: 'var(--font-size-3)',
      textAlign: 'center',
      fontWeight: 400
   },
   menuCloudIcon: {
      height: '90%',
      opacity: '0.85'
   }
}
