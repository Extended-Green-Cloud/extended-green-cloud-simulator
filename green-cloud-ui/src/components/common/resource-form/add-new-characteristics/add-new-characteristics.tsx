import { AddWithInput } from 'components/common'
import { Resource, ResourceCharacteristic } from '@types'
import { UpdateResource } from '../resource-form'
import { UpdateNumeric } from '../resource-configuration/resource-configuration'

interface Props {
   resource: Resource
   resourceName: string
   setNewResources: UpdateResource
   setNumericResources: UpdateNumeric
}

const getEmptyCharacteristic = (): ResourceCharacteristic => ({
   value: '',
   unit: '',
   toCommonUnitConverter: '',
   fromCommonUnitConverter: '',
   resourceCharacteristicReservation: '',
   resourceCharacteristicAddition: ''
})

/**
 * Component representing form used to add new resource characteristics
 *
 * @param {Resource}[resource] - the resource for which characteristic is to be added
 * @param {string}[resourceName] - name of the resource for which characteristic is to be added
 * @param {UpdateResource}[setNewResources] - function used to update resource map
 * @param {UpdateNumeric}[setNumericResources] - function used to update resource type assignment map
 * @returns JSX Element
 */
const AddNewResourceCharacteristic = ({ resource, resourceName, setNewResources, setNumericResources }: Props) => {
   const addEmptyResourceCharacteristic = (inputName: string) => {
      setNewResources((prevState) => {
         const emptyCharacteristic = getEmptyCharacteristic()
         const newCharacteristics = {
            ...prevState[resourceName]?.characteristics,
            [inputName]: emptyCharacteristic
         }
         return {
            ...prevState,
            [resourceName]: {
               ...prevState[resourceName],
               emptyResource:
                  prevState[resourceName].emptyResource !== null
                     ? {
                          ...(prevState[resourceName].emptyResource as Resource),
                          characteristics: {
                             ...(prevState[resourceName].emptyResource as Resource).characteristics,
                             [inputName]: emptyCharacteristic
                          }
                       }
                     : null,
               characteristics: newCharacteristics
            }
         }
      })
      setNumericResources((prevState) => prevState.concat({ isNumeric: false, key: resourceName, keyC: inputName }))
   }

   return (
      <AddWithInput
         {...{
            handleButtonPress: addEmptyResourceCharacteristic,
            inputTitle: 'Provide new characteristic name',
            buttonTitle: 'Add new characteristic',
            isDisabled: (name: string) => Object.keys(resource.characteristics).includes(name)
         }}
      />
   )
}

export default AddNewResourceCharacteristic
