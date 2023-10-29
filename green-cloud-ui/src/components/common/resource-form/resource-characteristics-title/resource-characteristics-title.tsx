import { Button } from 'components/common'
import { styles } from './resource-characteristics-title-styles'
import { UpdateResource } from '../resource-configuration/resource-configuration'
import { IconCross } from '@assets'
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
   const { wrapper, text } = styles

   const deleteResourceCharacteristic = (key: string, keyC: string) => {
      setNewResources((prevState) => {
         const newCharacteristics = { ...prevState[key]?.characteristics }
         delete newCharacteristics[keyC]

         let emptyResourceCharacteristics: { [key: string]: ResourceCharacteristic } = {}

         if (prevState[key].emptyResource !== null) {
            emptyResourceCharacteristics = { ...prevState[key].emptyResource?.characteristics }
            delete emptyResourceCharacteristics[keyC]
         }

         return {
            ...prevState,
            [key]: {
               ...prevState[key],
               characteristics: newCharacteristics,
               emptyResource:
                  prevState[key].emptyResource !== null
                     ? {
                          ...(prevState[key].emptyResource as Resource),
                          characteristics: emptyResourceCharacteristics
                       }
                     : null
            }
         }
      })
   }

   return (
      <div style={wrapper}>
         {!isEmpty && (
            <Button
               {...{
                  title: <IconCross size="20px" color="var(--gray-3)" />,
                  onClick: () => deleteResourceCharacteristic(resourceName, characteristicName),
                  buttonClassName: ''
               }}
            />
         )}
         <div style={text}>{characteristicName}</div>
      </div>
   )
}

export default ResourceCharacteristicTitle
