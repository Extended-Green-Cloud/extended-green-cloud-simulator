import React, { useState } from 'react'
import { Button, InputField } from 'components/common'
import { styles } from './add-new-resource-styles'
import { Resource } from '@types'
import { UpdateResource } from '../resource-form'

interface Props {
   setNewResources: UpdateResource
}

const getEmptyResource = (): Resource => ({
   characteristics: {},
   emptyResource: {
      characteristics: {},
      emptyResource: null,
      sufficiencyValidator: '',
      resourceAddition: '',
      resourceComparator: ''
   },
   sufficiencyValidator: '',
   resourceAddition: '',
   resourceComparator: ''
})

/**
 * Component representing form used to add new resource
 *
 * @param {UpdateResource}[setNewResources] - function used to update resource map
 * @returns JSX Element
 */
const AddNewResource = ({ setNewResources }: Props) => {
   const [newResourceName, setNewResourceName] = useState<string>('')
   const { newResourceWrapper, newResourceText, newResourceButton } = styles
   const addNewResourceButton = ['medium-green-button-active', 'medium-green-button', 'full-width-button'].join(' ')

   const isNameEmpty = newResourceName === ''

   const addEmptyResource = () => {
      setNewResources((prevState) => {
         const emptyResource = getEmptyResource()
         return {
            ...prevState,
            [newResourceName]: emptyResource
         }
      })
      setNewResourceName('')
   }

   return (
      <div style={newResourceWrapper}>
         <div style={newResourceText}>
            <InputField
               {...{
                  placeholder: 'Provide new resource name',
                  value: newResourceName,
                  handleChange: (event: React.ChangeEvent<HTMLInputElement>) => setNewResourceName(event.target.value)
               }}
            />
         </div>
         <div style={newResourceButton}>
            <Button
               {...{
                  title: 'ADD NEW RESOURCE',
                  onClick: () => addEmptyResource(),
                  buttonClassName: addNewResourceButton,
                  isDisabled: isNameEmpty
               }}
            />
         </div>
      </div>
   )
}

export default AddNewResource
