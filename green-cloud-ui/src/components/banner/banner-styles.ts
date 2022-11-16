import React from 'react'

interface Styles {
   parentContainer: React.CSSProperties
   banerContent: React.CSSProperties
   logoContainer: React.CSSProperties
   bannerText: React.CSSProperties
   bannerCloudIcon: React.CSSProperties
   bannerMenuIcon: React.CSSProperties
}

export const styles: Styles = {
   parentContainer: {
      backgroundColor: 'var(--green-1)',
      boxShadow: 'var(--banner-shadow)',
      height: 'min-content',
      paddingBottom: '10%',
   },
   banerContent: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
   },
   bannerMenuIcon: {
      height: '55px',
      opacity: 0.95,
   },
   logoContainer: {
      display: 'flex',
      alignItems: 'center',
      color: 'var(--white)',
      fontFamily: 'var(--font-1)',
      fontSize: 'var(--font-size-1)',
      fontWeight: '300',
      paddingLeft: '10px',
      paddingTop: '10px',
      height: '50px',
   },
   bannerText: {
      paddingLeft: '15px',
      opacity: '0.85',
   },
   bannerCloudIcon: {
      height: '90%',
      opacity: '0.85',
   },
}
