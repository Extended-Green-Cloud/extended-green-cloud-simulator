import { HeaderWithDelete } from 'components/common'
import { UpdateResource } from '../resource-configuration/resource-configuration'
import { Resource, ResourceCharacteristic } from '@types'

interface Props {
   resourceName: string
   characteristicName: string
   setNewResources: UpdateResource
   isEmpty?: boolean
}

/**
 * Component wrapping title of resource characteristic
 *
 * @param {string}[resourceName] - name of the resource
 * @param {string}[resourceName] - name of the resource characteristic
 * @param {UpdateResource}[setNewResources] - function used to update resource map
 * @returns JSX Element
 */
const ResourceCharacteristicTitle = ({ resourceName, characteristicName, setNewResources, isEmpty }: Props) => {
   const deleteResourceCharacteristic = () => {
      setNewResources((prevState) => {
         const newCharacteristics = { ...prevState[resourceName]?.characteristics }
         delete newCharacteristics[characteristicName]

         let emptyResourceCharacteristics: { [key: string]: ResourceCharacteristic } = {}

         if (prevState[resourceName].emptyResource !== null) {
            emptyResourceCharacteristics = { ...prevState[resourceName].emptyResource?.characteristics }
            delete emptyResourceCharacteristics[characteristicName]
         }

         return {
            ...prevState,
            [resourceName]: {
               ...prevState[resourceName],
               characteristics: newCharacteristics,
               emptyResource:
                  prevState[resourceName].emptyResource !== null
                     ? {
                          ...(prevState[resourceName].emptyResource as Resource),
                          characteristics: emptyResourceCharacteristics
                       }
                     : null
            }
         }
      })
   }

   return (
      <HeaderWithDelete
         {...{ title: characteristicName, deleteFunction: deleteResourceCharacteristic, omitDelete: isEmpty }}
      />
   )
}

export default ResourceCharacteristicTitle
