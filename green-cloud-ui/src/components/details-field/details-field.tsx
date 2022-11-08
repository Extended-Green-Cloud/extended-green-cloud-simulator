import React from 'react'
import { styles } from './details-field-style'

interface Props {
   label: string
   value?: any
   valueObject?: React.ReactNode
}

/**
 * Component represents a single details fiels presented on statistics panels
 *
 * @param {string}[label] - label describing the value
 * @param {any}[value] - value of given field
 * @param {object}[valueObject] - object displayed instead of the text node
 * @returns
 */
const DetailsField = ({ label, value, valueObject }: Props) => {
   const textStyle = { ...styles.value, ...styles.valueText }

   const getValue = () =>
      typeof valueObject !== 'undefined' ? (
         <div style={styles.value}>{valueObject}</div>
      ) : (
         <div style={textStyle}>{value.toString().toUpperCase()}</div>
      )

   return (
      <div style={styles.detailsContainer}>
         <div style={styles.label}>{label.toUpperCase()}</div>
         {getValue()}
      </div>
   )
}

export default DetailsField
