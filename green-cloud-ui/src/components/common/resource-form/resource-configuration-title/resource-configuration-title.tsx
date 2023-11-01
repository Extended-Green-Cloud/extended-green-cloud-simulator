import { HeaderWithDelete } from 'components/common'
import { UpdateResource } from '../resource-configuration/resource-configuration'

interface Props {
   resourceName: string
   setNewResources: UpdateResource
}

/**
 * Component wrapping title of resource
 *
 * @param {string}[resourceName] - name of the resource
 * @param {UpdateResource}[setNewResources] - function used to update resource map
 * @returns JSX Element
 */
const ResourceTitle = ({ resourceName, setNewResources }: Props) => {
   const deleteResource = () => {
      setNewResources((prevState) => {
         const newResources = { ...prevState }
         delete newResources[resourceName]
         return { ...newResources }
      })
   }

   return <HeaderWithDelete {...{ title: resourceName, deleteFunction: deleteResource }} />
}

export default ResourceTitle
