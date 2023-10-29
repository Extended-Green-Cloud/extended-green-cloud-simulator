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
   resetResource: boolean
   setResetResource: UpdateResourceReset
   newResources: ResourceMap
   setNewResources: UpdateResource
   initialNumericResources?: NumericResources[]
   skipEmptyResource?: boolean
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

export type UpdateResource = (value: React.SetStateAction<ResourceMap>) => void
export type UpdateNumeric = (value: React.SetStateAction<NumericResources[]>) => void

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
   skipDropdown,
   isEmpty
}: Props) => {
   const { resourceWrapper, resourceContent, resourceTrigger } = styles
   const [numericResources, setNumericResources] = useState<NumericResources[]>(
      initialNumericResources ?? getDefaultNumerics(newResources)
   )

   useEffect(() => {
      if (resetResource) {
         setNumericResources(initialNumericResources ?? getDefaultNumerics(newResources))
         setResetResource(false)
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
               <div style={{ borderLeft: 'var(--border-bold-gray-1)', borderRadius: 8, padding: '5px 10px' }}>
                  <ResourceConfiguration
                     {...{
                        resourceName,
                        resource: resource.emptyResource,
                        setResetResource,
                        resetResource,
                        newResources,
                        setNewResources,
                        initialNumericResources: numericResources,
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
                     isEmpty
                  }}
               />
               {getEmptyResource()}
               {getTextConfiguration(
                  'Resource aggregator',
                  'resourceAddition',
                  'Please provide function used to add two equivalent resources'
               )}
               {getTextConfiguration(
                  'Resource comparator',
                  'resourceComparator',
                  'Please provide function used to compare resources'
               )}
               {getTextConfiguration(
                  'Resource sufficiency evaluator',
                  'sufficiencyValidator',
                  'Please provide function used to evaluate if resources are sufficient'
               )}
            </>
         )
      }
      return <AddNewResource {...{ setNewResources }} />
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
