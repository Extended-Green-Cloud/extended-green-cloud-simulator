import { useState, useEffect } from 'react'
import { ResourceMap, Resource } from '@types'
import { Collapse, InputField } from 'components/common'
import { styles } from './resource-configuration-styles'
import ResourceConfigurationField from '../resource-configuration-field/resource-configuration-field'
import ResourceConfigurationCharacteristics from '../resource-characteristics/resource-characteristics'
import AddNewResource from '../add-new-resource/add-new-resource'
import ResourceTitle from '../resource-configuration-title/resource-configuration-title'
import { UpdateResourceReset } from '../resource-form'

interface Props {
   resourceName: string
   resource: Resource | null
   resetResource?: boolean
   setResetResource?: UpdateResourceReset
   newResources: ResourceMap
   setNewResources: UpdateResource
   initialNumericResources?: NumericResources[]
   skipEmptyResource?: boolean
   skipFunctionDefinition?: boolean
   skipDropdown?: boolean
   isEmpty?: boolean
}

const getDefaultNumerics = (resourceMap: ResourceMap) =>
   Object.entries(resourceMap).flatMap(([key, resource]) =>
      Object.entries(resource.characteristics).map(
         ([keyC, _]) => ({ key, keyC, isNumeric: typeof _.value === 'number' } as NumericResources)
      )
   )

export type NumericResources = {
   key: string
   keyC: string
   isNumeric: boolean
}

export type UpdateResource = React.Dispatch<React.SetStateAction<ResourceMap>>
export type UpdateNumeric = React.Dispatch<React.SetStateAction<NumericResources[]>>

/**
 * Component allowing to configure single resource
 *
 * @param {string}[resourceName] - name of the resource
 * @param {Resource}[resource] - resource which is to be configured
 * @param {boolean}[resetResource] - flag indicating if resources were reset to prior form
 * @param {UpdateResource}[setResetResource] - function used to update resource reset state
 * @param {ResourceMap}[newResources] - new values of resources
 * @param {UpdateResource}[setNewResources] = function used to update resource values
 * @param {NumericResources[]}[initialNumericResources] - optionally passed assignment of resource types
 * @param {boolean}[skipEmptyResource] - parameter specifying if the empty resource component should be skipped
 * @param {boolean}[skipDropdown] - optional flag indicating if the dropdown should be skipped
 * @param {boolean}[isEmpty] - flag indicating if the resource represents nested empty resource
 * @param {boolean}[skipFunctionDefinition] - optional flag indicating if definition of resource handling methods should be skipped
 *
 * @returns JSX Element
 */
const ResourceConfiguration = ({
   resourceName,
   resource,
   resetResource,
   newResources,
   setNewResources,
   setResetResource,
   initialNumericResources,
   skipEmptyResource,
   skipFunctionDefinition,
   skipDropdown,
   isEmpty
}: Props) => {
   const { resourceWrapper, resourceContent, resourceTrigger, resourceFieldWrapper } = styles
   const [numericResources, setNumericResources] = useState<NumericResources[]>(
      initialNumericResources ?? getDefaultNumerics(newResources)
   )

   useEffect(() => {
      if (resetResource) {
         setNumericResources(initialNumericResources ?? getDefaultNumerics(newResources))

         if (setResetResource) {
            setResetResource(false)
         }
      }
   }, [resetResource])

   const changeResourceValue = (fieldName: string, newValue: string) => {
      setNewResources((prevState) => ({
         ...prevState,
         [resourceName]: {
            ...prevState[resourceName],
            [fieldName]: newValue,
            emptyResource:
               prevState[resourceName].emptyResource !== null
                  ? {
                       ...(prevState[resourceName].emptyResource as Resource),
                       [fieldName]: newValue
                    }
                  : null
         }
      }))
   }

   const changeEmptyResourceValue = (fieldName: string, newValue: string) => {
      setNewResources((prevState) => ({
         ...prevState,
         [resourceName]: {
            ...prevState[resourceName],
            emptyResource:
               prevState[resourceName].emptyResource !== null
                  ? {
                       ...(prevState[resourceName].emptyResource as Resource),
                       [fieldName]: newValue
                    }
                  : null
         }
      }))
   }

   const changeValue = isEmpty ? changeEmptyResourceValue : changeResourceValue

   const getTextConfiguration = (label: string, fieldName: keyof Resource, placeholder: string) => {
      if (resource) {
         return (
            <ResourceConfigurationField fieldName={label}>
               <InputField
                  {...{
                     isTextField: true,
                     useCodeFormatter: true,
                     handleChange: (event: React.ChangeEvent<HTMLTextAreaElement>) =>
                        changeValue(fieldName, event.target.value),
                     value: resource[fieldName] as string,
                     placeholder
                  }}
               />
            </ResourceConfigurationField>
         )
      }
   }

   const getEmptyResource = () => {
      if (!skipEmptyResource && resource) {
         return (
            <ResourceConfigurationField fieldName="Empty Resource Representation">
               <div style={resourceFieldWrapper}>
                  <ResourceConfiguration
                     {...{
                        resourceName,
                        resource: resource.emptyResource,
                        setResetResource,
                        resetResource,
                        newResources,
                        setNewResources,
                        initialNumericResources: numericResources,
                        skipFunctionDefinition,
                        skipEmptyResource: true,
                        skipDropdown: true,
                        isEmpty: true
                     }}
                  />
               </div>
            </ResourceConfigurationField>
         )
      }
   }

   const getResourceCharacteristics = () => {
      if (resource) {
         return (
            <>
               <ResourceConfigurationCharacteristics
                  {...{
                     resourceName,
                     resource,
                     newResources,
                     setNewResources,
                     numericResources: initialNumericResources ?? numericResources,
                     setNumericResources,
                     skipFunctionDefinition,
                     skipEmptyResource,
                     isEmpty
                  }}
               />
               {getEmptyResource()}
               {!skipFunctionDefinition &&
                  getTextConfiguration(
                     'Resource comparator',
                     'resourceComparator',
                     'Please provide function used to compare resources'
                  )}
               {!skipFunctionDefinition &&
                  getTextConfiguration(
                     'Resource sufficiency evaluator',
                     'resourceValidator',
                     'Please provide function used to evaluate if resources are sufficient'
                  )}
            </>
         )
      }
      return <AddNewResource {...{ setNewResources, skipEmptyResource, resources: newResources }} />
   }

   return (
      <>
         {skipDropdown ? (
            getResourceCharacteristics()
         ) : (
            <Collapse
               {...{
                  title: <ResourceTitle {...{ resourceName, setNewResources }} />,
                  triggerStyle: resourceTrigger,
                  wrapperStyle: resourceWrapper,
                  contentStyle: resourceContent
               }}
            >
               {getResourceCharacteristics()}
            </Collapse>
         )}
      </>
   )
}

export default ResourceConfiguration
