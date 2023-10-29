import { ResourceMap, Resource } from '@types'
import { Collapse, SingleCheckBox, InputField } from 'components/common'
import ResourceConfigurationField from '../resource-configuration-field/resource-configuration-field'
import AddNewResourceCharacteristic from '../add-new-characteristics/add-new-characteristics'
import ResourceCharacteristicTitle from '../resource-characteristics-title/resource-characteristics-title'
import ResourceCharacteristicField from '../resource-characteristic-field/resource-characteristic-field'
import { styles } from './resource-characteristics-styles'
import { NumericResources, UpdateNumeric, UpdateResource } from '../resource-configuration/resource-configuration'

interface Props {
   resourceName: string
   resource: Resource
   newResources: ResourceMap
   setNewResources: UpdateResource
   numericResources: NumericResources[]
   setNumericResources: UpdateNumeric
   isEmpty?: boolean
}

/**
 * Component allowing to configure single resource characteristics
 *
 * @param {string}[resourceName] - name of the resource
 * @param {Resource}[resource] - resource which is to be configured
 * @param {ResourceMap}[newResources] - new values of resources
 * @param {UpdateResource}[setNewResources] - function used to update resource values
 * @param {NumericResources[]}[numericResources] - assignment of resource types
 * @param {UpdateNumeric}[setNumericResources] - function changing assignment of resource types
 * @param {boolean}[isEmpty] - flag indicating if the resource represents nested empty resource
 *
 * @returns JSX Element
 */
const ResourceConfigurationCharacteristics = ({
   resourceName,
   resource,
   setNewResources,
   numericResources,
   setNumericResources,
   isEmpty
}: Props) => {
   const { characteristicFieldTrigger, characteristicFieldWrapper } = styles

   const isNumeric = (keyC: string) =>
      numericResources.find((entry) => entry.key === resourceName && entry.keyC === keyC)?.isNumeric ?? false

   const changeResourceCharacteristicNumeric = (isSelected: boolean, keyC: string, newValue: any) => {
      const newMap = numericResources.map((entry) =>
         entry.key === resourceName && entry.keyC === keyC ? { ...entry, isNumeric: !isSelected } : entry
      )
      setNumericResources(newMap)

      const isTypeNotCompatible = !isSelected && !/^[0-9]*[.,]?[0-9]*$/.test(newValue)
      const valueToUse = isTypeNotCompatible ? 0 : newValue
      changeResourceCharacteristicValue(keyC, 'value', valueToUse)
   }

   const changeResourceCharacteristicValue = (keyC: string, field: string, newValue: any) => {
      setNewResources((prevState) => ({
         ...prevState,
         [resourceName]: {
            ...prevState[resourceName],
            characteristics: {
               ...prevState[resourceName].characteristics,
               [keyC]: {
                  ...prevState[resourceName].characteristics[keyC],
                  [field]: field === 'value' && isNumeric(keyC) ? +newValue : `${newValue}`
               }
            }
         }
      }))
   }

   const changeEmptyResourceCharacteristicValue = (keyC: string, field: string, newValue: any) => {
      setNewResources((prevState) => ({
         ...prevState,
         [resourceName]: {
            ...prevState[resourceName],
            emptyResource: {
               ...(prevState[resourceName].emptyResource as Resource),
               characteristics: {
                  [keyC]: {
                     ...(prevState[resourceName].emptyResource as Resource).characteristics[keyC],
                     [field]: field === 'value' && isNumeric(keyC) ? +newValue : `${newValue}`
                  }
               }
            }
         }
      }))
   }

   const changeResourceAndEmptyCharacteristicValue = (keyC: string, field: string, newValue: any) => {
      setNewResources((prevState) => ({
         ...prevState,
         [resourceName]: {
            ...prevState[resourceName],
            characteristics: {
               ...prevState[resourceName].characteristics,
               [keyC]: {
                  ...prevState[resourceName].characteristics[keyC],
                  [field]: field === 'value' && isNumeric(keyC) ? +newValue : `${newValue}`
               }
            },
            emptyResource: {
               ...(prevState[resourceName].emptyResource as Resource),
               characteristics: {
                  [keyC]: {
                     ...(prevState[resourceName].emptyResource as Resource).characteristics[keyC],
                     [field]: field === 'value' && isNumeric(keyC) ? +newValue : `${newValue}`
                  }
               }
            }
         }
      }))
   }

   const changeValue = isEmpty ? changeEmptyResourceCharacteristicValue : changeResourceCharacteristicValue
   const changeCommon = isEmpty ? changeEmptyResourceCharacteristicValue : changeResourceAndEmptyCharacteristicValue

   const getValueField = (value: any, keyC: string) => (
      <ResourceCharacteristicField {...{ propertyName: 'value' }}>
         <InputField
            {...{
               placeholder: 'Provide resource characteristic value',
               isNumeric: isNumeric(keyC),
               handleChange: (event: React.ChangeEvent<HTMLInputElement>) =>
                  changeValue(keyC, 'value', event.target.value),
               value
            }}
         />
         <div style={{ marginTop: '5px' }}>
            <SingleCheckBox
               {...{
                  option: {
                     value: 'NUMERIC TYPE',
                     label: 'NUMERIC TYPE',
                     isSelected: isNumeric(keyC)
                  },
                  disabled: isEmpty,
                  onChange: (_, isSelected) => changeResourceCharacteristicNumeric(isSelected, keyC, value)
               }}
            />
         </div>
      </ResourceCharacteristicField>
   )

   const getTextField = (value: any, keyC: string, label: string, fieldName: string, placeholder: string) => (
      <ResourceCharacteristicField {...{ propertyName: label }}>
         <InputField
            {...{
               isTextField: true,
               useCodeFormatter: true,
               handleChange: (event: React.ChangeEvent<HTMLTextAreaElement>) =>
                  changeCommon(keyC, fieldName, event.target.value),
               value,
               placeholder
            }}
         />
      </ResourceCharacteristicField>
   )

   return (
      <ResourceConfigurationField fieldName="Resource Characteristics">
         {!isEmpty && <AddNewResourceCharacteristic {...{ resourceName, setNewResources, setNumericResources }} />}
         {Object.entries(resource.characteristics).map(([keyC, resourceC]) => (
            <Collapse
               {...{
                  title: (
                     <ResourceCharacteristicTitle
                        {...{ resourceName, characteristicName: keyC, setNewResources, isEmpty }}
                     />
                  ),
                  wrapperStyle: characteristicFieldWrapper,
                  triggerStyle: characteristicFieldTrigger,
                  contentStyle: { paddingBottom: '10px' }
               }}
            >
               {getValueField(resourceC.value, keyC)}
               {getTextField(resourceC.unit, keyC, 'unit', 'unit', 'Provide the unit')}
               {getTextField(
                  resourceC.toCommonUnitConverter,
                  keyC,
                  'Convert to common unit',
                  'toCommonUnitConverter',
                  'Provide to common unit converter in Expression Language'
               )}
               {getTextField(
                  resourceC.fromCommonUnitConverter,
                  keyC,
                  'Convert from common unit',
                  'fromCommonUnitConverter',
                  'Provide from common unit converter in Expression Language'
               )}
               {getTextField(
                  resourceC.resourceBooker,
                  keyC,
                  'Reserve resource',
                  'resourceBooker',
                  'Provide method (in Expression Language) used to reserve resources'
               )}
            </Collapse>
         ))}
      </ResourceConfigurationField>
   )
}

export default ResourceConfigurationCharacteristics
