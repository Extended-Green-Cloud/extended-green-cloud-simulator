import { ResourceMap } from '@types'
import ResourceConfiguration from './resource-configuration/resource-configuration'
import AddNewResource from './add-new-resource/add-new-resource'

interface Props {
   resetResource: boolean
   newResources: ResourceMap
   setResetResource: UpdateResourceReset
   setNewResources: UpdateResource
}

export type UpdateResource = (value: React.SetStateAction<ResourceMap>) => void
export type UpdateResourceReset = (value: React.SetStateAction<boolean>) => void

/**
 * Component represents container that allows to configure resources
 *
 * @param {ResourceMap}[newResources] - resources
 * @param {UpdateResource}[setNewResources] - function used to update resources
 * @param {boolean}[resetResource] - flag indicating if resources were reset to prior form
 * @param {UpdateResource}[setResetResource] - function used to update resource reset state
 *
 * @returns JSX Element
 */
const ResourceForm = ({ newResources, setNewResources, resetResource, setResetResource }: Props) => {
   return (
      <>
         <AddNewResource {...{ setNewResources }} />
         {Object.entries(newResources).map(([key, resource]) => (
            <ResourceConfiguration
               {...{ resource, resourceName: key, newResources, setNewResources, resetResource, setResetResource }}
            />
         ))}
      </>
   )
}

export default ResourceForm
