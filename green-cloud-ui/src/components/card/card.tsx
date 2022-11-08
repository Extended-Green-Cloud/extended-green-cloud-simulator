import React from 'react'
import { styles } from './card-styles'

interface Props {
   children?: React.ReactNode
   header?: string | React.ReactNode
   removeScroll?: boolean
   containerStyle?: React.CSSProperties
   contentStyle?: React.CSSProperties
}

/**
 * Component representing common container with shadow effect
 *
 * @param {object}[children] - content to be displayed inside container
 * @param {string}[header] - header displayed at the top of the container
 * @param {boolean}[removeScroll] -flag indicating whether the scroll bar   should be removed
 * @param {object}[containerStyle] - optional styling applied to the container
 * @param {object}[contentStyle] - optional styling applied to the content
 *
 * @returns JSX Element
 */
const Card = ({
   header,
   children,
   containerStyle,
   contentStyle,
   removeScroll,
}: Props) => {
   const contentFinalStyle = removeScroll
      ? { ...styles.cardContent, ...contentStyle }
      : { ...styles.cardContent, ...styles.cardContentScroll }
   const parentContainerStyle = { ...styles.cardContainer, ...containerStyle }

   const mapHeader = () =>
      typeof header === 'string' ? (
         <div style={styles.cardHeader}>{header?.toUpperCase()}</div>
      ) : (
         header
      )

   return (
      <div style={parentContainerStyle}>
         {header && mapHeader()}
         <div style={contentFinalStyle}>{children}</div>
      </div>
   )
}

export default Card
