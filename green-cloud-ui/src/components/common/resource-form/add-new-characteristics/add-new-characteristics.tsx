import React, { useState } from 'react'
import { Button, InputField } from 'components/common'
import { styles } from './add-new-characteristics-styles'
import { Resource, ResourceCharacteristic } from '@types'
import { UpdateResource } from '../resource-form'
import { UpdateNumeric } from '../resource-configuration/resource-configuration'

interface Props {
   resourceName: string
   setNewResources: UpdateResource
   setNumericResources: UpdateNumeric
}

const getEmptyCharacteristic = (): ResourceCharacteristic => ({
   value: '',
   unit: '',
   toCommonUnitConverter: '',
   fromCommonUnitConverter: '',
   resourceBooker: ''
})

/**
 * Component representing form used to add new resource characteristics
 *
 * @param {string}[resourceName] - name of the resource for which characteristic is to be added
 * @param {UpdateResource}[setNewResources] - function used to update resource map
 * @param {UpdateNumeric}[setNumericResources] - function used to update resource type assignment map
 * @returns JSX Element
 */
const AddNewResourceCharacteristic = ({ resourceName, setNewResources, setNumericResources }: Props) => {
   const [newResourceName, setNewResourceName] = useState<string>('')
   const { newResourceWrapper, newResourceText, newResourceButton } = styles
   const addNewResourceButton = ['medium-green-button-active', 'medium-green-button', 'full-width-button'].join(' ')

   const isNameEmpty = newResourceName === ''

   const addEmptyResourceCharacteristic = (resourceCharacteristicName: string, key: string) => {
      setNewResources((prevState) => {
         const emptyCharacteristic = getEmptyCharacteristic()
         const newCharacteristics = {
            ...prevState[key]?.characteristics,
            [resourceCharacteristicName]: emptyCharacteristic
         }
         return {
            ...prevState,
            [key]: {
               ...prevState[key],
               emptyResource:
                  prevState[key].emptyResource !== null
                     ? {
                          ...(prevState[key].emptyResource as Resource),
                          characteristics: {
                             ...(prevState[key].emptyResource as Resource).characteristics,
                             [resourceCharacteristicName]: emptyCharacteristic
                          }
                       }
                     : null,
               characteristics: newCharacteristics
            }
         }
      })
      setNumericResources((prevState) =>
         prevState.concat({ isNumeric: false, key: resourceName, keyC: resourceCharacteristicName })
      )
      setNewResourceName('')
   }

   return (
      <div style={newResourceWrapper}>
         <div style={newResourceText}>
            <InputField
               {...{
                  placeholder: 'Provide new characteristic name',
                  value: newResourceName,
                  handleChange: (event: React.ChangeEvent<HTMLInputElement>) => setNewResourceName(event.target.value)
               }}
            />
         </div>
         <div style={newResourceButton}>
            <Button
               {...{
                  title: 'ADD NEW CHARACTERISTIC',
                  onClick: () => addEmptyResourceCharacteristic(newResourceName, resourceName),
                  buttonClassName: addNewResourceButton,
                  isDisabled: isNameEmpty
               }}
            />
         </div>
      </div>
   )
}

export default AddNewResourceCharacteristic
