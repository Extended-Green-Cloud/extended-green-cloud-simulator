import React from 'react'
import { CircularProgressbarStyles } from 'react-circular-progressbar/dist/types'

interface Styles {
   percentageIndicatorWrapper: React.CSSProperties
   percentageIndicatorTitle: React.CSSProperties
   percentageIndicatorContainer: React.CSSProperties
   percentageIndicatorContent: CircularProgressbarStyles
}

export const styles: Styles = {
   percentageIndicatorWrapper: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'space-around',
      width: '80%',
      height: '95%',
      padding: '15px 10px 10px 15px',
      minWidth: 'fit-content',
      boxShadow: 'var(--event-shadow)',
      borderRadius: 20,
      margin: '5px 0px'
   },
   percentageIndicatorTitle: {
      fontSize: '1vw',
      fontWeight: 500,
      color: 'var(--gray-3)',
      marginBottom: '25px',
      textAlign: 'left',
      width: '100%'
   },
   percentageIndicatorContainer: {
      width: '9vw',
      display: 'flex',
      flexShrink: 0,
      paddingBottom: '10px'
   },
   percentageIndicatorContent: {
      path: {
         stroke: `var(--green-1)`,
         strokeLinecap: 'round'
      },
      trail: {
         stroke: 'var(--gray-5)',
         strokeLinecap: 'round',
         transformOrigin: 'center center'
      },
      text: {
         fill: 'var(--gray-3)',
         fontWeight: 500,
         fontSize: 'var(--font-size-8)',
         textAnchor: 'middle',
         alignmentBaseline: 'middle',
         transformOrigin: 'center center'
      }
   }
}
