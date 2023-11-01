import { ResourceMap } from '@types'
import ResourceConfiguration from './resource-configuration/resource-configuration'
import AddNewResource from './add-new-resource/add-new-resource'
import UploadJSONButton from '../upload-json-button/upload-json-button'

interface Props {
   resetResource?: boolean
   newResources: ResourceMap
   skipEmptyResource?: boolean
   skipFunctionDefinition?: boolean
   setResetResource?: UpdateResourceReset
   setNewResources: UpdateResource
}

export type UpdateResource = (value: React.SetStateAction<ResourceMap> | ResourceMap) => void
export type UpdateResourceReset = (value: React.SetStateAction<boolean>) => void

/**
 * Component represents container that allows to configure resources
 *
 * @param {ResourceMap}[newResources] - resources
 * @param {UpdateResource}[setNewResources] - function used to update resources
 * @param {boolean}[resetResource] - flag indicating if resources were reset to prior form
 * @param {UpdateResource}[setResetResource] - function used to update resource reset state
 * @param {boolean}[skipEmptyResource] - optional flag indicating if empty resource should be skipped
 * @param {boolean}[skipFunctionDefinition] - optional flag indicating if definition of resource handling methods should be skipped
 *
 * @returns JSX Element
 */
const ResourceForm = ({
   newResources,
   setNewResources,
   resetResource,
   setResetResource,
   skipEmptyResource,
   skipFunctionDefinition
}: Props) => {
   return (
      <>
         <UploadJSONButton {...{ buttonText: 'Upload resources', handleUploadedContent: setNewResources }} />
         <AddNewResource {...{ setNewResources, skipEmptyResource, resources: newResources }} />
         {Object.entries(newResources).map(([key, resource]) => (
            <ResourceConfiguration
               {...{
                  resource,
                  resourceName: key,
                  newResources,
                  setNewResources,
                  resetResource,
                  setResetResource,
                  skipEmptyResource,
                  skipFunctionDefinition
               }}
            />
         ))}
      </>
   )
}

export default ResourceForm
