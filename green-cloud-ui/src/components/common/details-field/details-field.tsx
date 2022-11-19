import React from 'react'
import { styles } from './details-field-style'

interface Props {
   label: string | React.ReactNode
   value?: any
   valueObject?: React.ReactNode
   isHeader?: boolean
   fieldContainerStyle?: React.CSSProperties
   fieldValueStyle?: React.CSSProperties
   fieldLabelStyle?: React.CSSProperties
}

/**
 * Component represents a single details field
 *
 * @param {string}[label] - label describing the value
 * @param {any}[value] - value of given field
 * @param {object}[valueObject] - object displayed instead of the text node
 * @param {boolean}[isHeader] - flag indicating if the value should have additional header styling
 * @param {React.CSSProperties}[fieldContainerStyle] - optional field container style
 * @param {React.CSSProperties}[fieldValueStyle] - optional field label style
 * @param {React.CSSProperties}[fieldLabelStyle] - optional field value style
 * @returns
 */
const DetailsField = ({
   label,
   value,
   valueObject,
   fieldContainerStyle,
   fieldValueStyle,
   fieldLabelStyle,
   isHeader = false,
}: Props) => {
   const { valueText, headerLabel, detailsContainer, headerContainer } = styles

   const textStyle = { ...styles.value, ...valueText }
   const labelStyle = !isHeader
      ? styles.label
      : { ...styles.label, ...headerLabel }
   const containerStyle = !isHeader
      ? detailsContainer
      : { ...detailsContainer, ...headerContainer }

   const getValue = () =>
      typeof valueObject !== 'undefined' ? (
         <div style={{ ...styles.value, ...fieldValueStyle }}>
            {valueObject}
         </div>
      ) : (
         <div style={{ ...textStyle, ...fieldValueStyle }}>
            {value.toString().toUpperCase()}
         </div>
      )

   return (
      <div style={{ ...containerStyle, ...fieldContainerStyle }}>
         <div style={{ ...labelStyle, ...fieldLabelStyle }}>
            {typeof label === 'string' ? label.toUpperCase() : label}
         </div>
         {(value !== undefined || valueObject !== undefined) && getValue()}
      </div>
   )
}

export default DetailsField
