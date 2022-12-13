import { DetailField } from '@types'
import React from 'react'
import { styles } from './details-card-styles'

interface Props {
   fieldMap: DetailField[]
   title: string
   objectMap: any
}

/**
 * Component represents a details card with title
 *
 * @param {DetailField[]}[fieldMap] - map describing fields that will be desplayed in the card
 * @param {string}[title] - title of the card
 * @param {any}[objectMap] - object containing properties that will be displayed on the card
 * @returns
 */
const DetailsCard = ({ fieldMap, title, objectMap }: Props) => {
   const { container, fieldWrapper, fieldLabel, fieldValue, titleStyle } =
      styles

   const parseField = (label: string, key: string) => {
      const value = { ...objectMap }[key]
      return (
         value !== undefined &&
         value !== null && (
            <div style={fieldWrapper}>
               <div style={fieldLabel}>{label}</div>
               <div style={fieldValue}>{value}</div>
            </div>
         )
      )
   }

   return (
      <div style={container}>
         <div style={titleStyle}>{title}</div>
         <div>
            {fieldMap.map((field) => parseField(field.label, field.key))}
         </div>
      </div>
   )
}

export default DetailsCard
