import React from 'react'
import { MultiLevelDetails, MultiLevelSubEntries } from '@types'
import { styles } from './multi-level-details-field-styles'
import Collapse from '../collapse/collapse'
import DetailsField from '../details-field/details-field'

interface Props {
   detailsFieldMap: MultiLevelDetails[]
}
/**
 * Component represents a container that display nested details fields
 *
 * @param {MultiLevelDetails[]}[detailsFieldMap] - map with nested fields to be displayed
 * @returns JSX Element
 */
const MultiLevelDetailsField = ({ detailsFieldMap }: Props) => {
   const { mainFieldWrapper, dropdownWrapper, dropdownContent, dropdownTrigger, subEntryWrapper } = styles

   const getSubEntry = (subEntry: MultiLevelSubEntries) => (
      <div>
         <div style={subEntryWrapper}>{subEntry.key.toUpperCase()}</div>
         <div>
            {subEntry.fields.map((field) => (
               <DetailsField key={field.label} {...{ ...field }} />
            ))}
         </div>
      </div>
   )

   return (
      <>
         {detailsFieldMap.map((mainType) => {
            return (
               <div>
                  <div style={mainFieldWrapper}>
                     <Collapse
                        title={mainType.key.toUpperCase()}
                        wrapperStyle={dropdownWrapper}
                        contentStyle={dropdownContent}
                        triggerStyle={dropdownTrigger}
                     >
                        <div>{mainType.fields.map((subEntry) => getSubEntry(subEntry))}</div>
                     </Collapse>
                  </div>
               </div>
            )
         })}
      </>
   )
}

export default MultiLevelDetailsField
