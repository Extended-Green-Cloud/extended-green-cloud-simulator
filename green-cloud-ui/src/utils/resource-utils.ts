/* eslint-disable @typescript-eslint/no-unused-vars */
import {
   MultiLevelDetails,
   MultiLevelSubEntries,
   MultiLevelValues,
   Resource,
   ResourceCharacteristic,
   ResourceCharacteristicDisplay,
   ResourceMap,
   ResourceMapper
} from '@types'

const mapResourceCharacteristic = (
   keyC: string,
   resourceC: ResourceCharacteristic,
   resourceMapper: ResourceCharacteristicDisplay[],
   inUseResourceC?: ResourceCharacteristic
) => {
   const fields = resourceMapper.map((entry) => {
      const displayedValue = inUseResourceC
         ? entry.mapper(resourceC, inUseResourceC)
         : (entry.mapper as ResourceMapper)(resourceC)
      return { label: entry.label, value: displayedValue } as MultiLevelValues
   })
   return { key: keyC, fields } as MultiLevelSubEntries
}

/**
 * Method returns parsed resources with units
 *
 * @param {ResourceCharacteristic} resourceC resource characteristic
 *
 * @returns string representing in use resources
 */
export const mapIValueWithUnit = (resourceC: ResourceCharacteristic) => {
   const isNumeric = typeof resourceC.value === 'number'
   const resourceCVal = isNumeric ? resourceC.value.toFixed(2) : resourceC.value
   return `${resourceCVal} ${resourceC.unit}`
}

/**
 * Method returns parsed in use resources
 *
 * @param {ResourceCharacteristic} resourceC resource characteristic
 * @param {ResourceCharacteristic} inUseResourceC in use resource
 *
 * @returns string representing in use resources
 */
export const mapInUseValues = (resourceC: ResourceCharacteristic, inUseResourceC: ResourceCharacteristic) => {
   const isNumeric = typeof resourceC.value === 'number'
   const resourceCVal = isNumeric ? resourceC.value.toFixed(2) : resourceC.value
   const inUseResourceCVal = isNumeric
      ? ((inUseResourceC?.value ?? 0) as number).toFixed(2)
      : inUseResourceC?.value ?? '-'

   return `${inUseResourceCVal} ${inUseResourceC?.unit ?? resourceC?.unit ?? ''} \\ ${resourceCVal} ${
      resourceC.unit ?? ''
   }`
}

/**
 * Method collects resources to a single structure.
 *
 * @param {ResourceMap} resources resources
 * @param {ResourceMap} [inUseResources] optional in use resource amounts
 * @param {ResourceCharacteristicDisplay[]} resourceMapper map defining how to parse individual resources
 *
 * @returns parsed value string
 */
export const collectResourcesToMultiMap = (
   resources: ResourceMap,
   resourceMapper: ResourceCharacteristicDisplay[],
   inUseResources?: ResourceMap
): MultiLevelDetails[] => {
   return Object.entries(resources).map(([key, resource]) => {
      const characteristics = resource.characteristics
         ? Object.entries(resource.characteristics).map(([keyC, resourceC]) => {
              const inUseResourceC = inUseResources ? inUseResources[key]?.characteristics[keyC] ?? {} : undefined
              return mapResourceCharacteristic(keyC, resourceC, resourceMapper, inUseResourceC)
           })
         : ([] as MultiLevelSubEntries[])
      return { key, fields: characteristics }
   })
}

/**
 * Method validates if CPU is among resources
 * @param resources resource map
 * @returns boolean
 */
export const validateCpuPresence = (resources: ResourceMap) => Object.keys(resources).find((key) => key === 'cpu')

/**
 * Method validates if CPU has amount
 * @param resources resource map
 * @returns boolean
 */
export const validateCpuAmount = (resources: ResourceMap) =>
   Object.keys(resources['cpu'].characteristics).some((keyC) => keyC === 'amount')

/**
 * Method validates if CPU has correct amount
 * @param resources resource map
 * @returns boolean
 */
export const validateCpuAmountValue = (resources: ResourceMap) =>
   validateCpuAmount(resources) && typeof resources['cpu'].characteristics['amount'].value !== 'number'

/**
 * Method looks for resources without characteristics
 * @param resources resource map
 * @returns list of incorrect resources
 */
export const getResourcesWithoutCharacteristics = (resources: ResourceMap) =>
   Object.entries(resources)
      .filter(([_, resource]) => Object.keys(resource.characteristics).length === 0)
      .map(([key, _]) => key)

/**
 * Method looks for characteristics without values
 * @param resources resource map
 * @param isEmpty flag specifies if empty resources should be checked
 * @returns list of incorrect characteristics
 */
export const getCharacteristicsWithoutValues = (resources: ResourceMap, isEmpty: boolean) =>
   Object.entries(resources)
      .filter(([_, resource]) => !isEmpty || resource.emptyResource)
      .flatMap(([key, resource]) =>
         Object.entries((isEmpty ? (resource.emptyResource as Resource) : resource).characteristics).map(
            ([keyC, resourceC]) => ({ key, keyC, value: resourceC.value })
         )
      )
      .filter((entry) => !entry.value && entry.value !== 0)
      .map((entry) => `${entry.key}->${entry.keyC}`)

/**
 * Method looks for characteristics with incorrect values
 * @param resources resource map
 * @param isEmpty flag specifies if empty resources should be checked
 * @returns list of incorrect characteristics
 */
export const getCharacteristicsWithIncorrectValues = (resources: ResourceMap, isEmpty: boolean) =>
   Object.entries(resources)
      .filter(([_, resource]) => !isEmpty || resource.emptyResource)
      .flatMap(([key, resource]) =>
         Object.entries((isEmpty ? (resource.emptyResource as Resource) : resource).characteristics).map(
            ([keyC, resourceC]) => ({ key, keyC, value: resourceC.value })
         )
      )
      .filter((entry) => typeof entry.value === 'number' && entry.value < 0)
      .map((entry) => `${entry.key}->${entry.keyC}`)

/**
 * Method looks for characteristics with incomplete converters
 * @param resources resource map
 * @param isEmpty flag specifies if empty resources should be checked
 * @returns list of incorrect characteristics
 */
export const getCharacteristicsWithMissingConverters = (resources: ResourceMap, isEmpty: boolean) =>
   Object.entries(resources)
      .filter(([_, resource]) => !isEmpty || resource.emptyResource)
      .flatMap(([key, resource]) =>
         Object.entries((isEmpty ? (resource.emptyResource as Resource) : resource).characteristics).map(
            ([keyC, resourceC]) => ({
               key,
               keyC,
               toConverter: resourceC.toCommonUnitConverter,
               fromConverter: resourceC.fromCommonUnitConverter
            })
         )
      )
      .filter((entry) => (!entry.toConverter && entry.fromConverter) || (entry.toConverter && !entry.fromConverter))
      .map((entry) => `${entry.key}->${entry.keyC}`)

/**
 * Method validates correctness of provided resource map
 * @param resources resource map
 * @returns string indicating ocurred error or empty string if resource map content is correct
 */
export const validateResources = (resources: ResourceMap) => {
   if (!validateCpuPresence(resources)) {
      return 'Resource "cpu" must be specified for each Server. Important! The case of letter matters.'
   }
   if (!validateCpuAmount(resources) || validateCpuAmountValue(resources)) {
      return 'Characteristic "amount" must be specified for "cpu" and must be of numeric type!'
   }
   const resourcesWithoutCharacteristics = getResourcesWithoutCharacteristics(resources)
   if (resourcesWithoutCharacteristics.length > 0) {
      return `There are resources for which no characteristics were defined! Resources without characteristics: 
               ${resourcesWithoutCharacteristics.join(', ')}`
   }
   const characteristicsWithoutValues = getCharacteristicsWithoutValues(resources, false)
   if (characteristicsWithoutValues.length > 0) {
      return `Values must be specified for all characteristics. Characteristics without values: 
               ${characteristicsWithoutValues.join(', ')}`
   }
   const characteristicsWithIncorrectValues = getCharacteristicsWithIncorrectValues(resources, false)
   if (characteristicsWithIncorrectValues.length > 0) {
      return `Values of resource characteristics must be greater than 0. Characteristics with incorrect values: 
               ${characteristicsWithIncorrectValues.join(', ')}`
   }
   const characteristicsWithMissingConverter = getCharacteristicsWithMissingConverters(resources, false)
   if (characteristicsWithMissingConverter.length > 0) {
      return `Either none or both unit converters must be specified for each characteristic. Characteristics with missing converters: 
               ${characteristicsWithMissingConverter.join(', ')}`
   }
   const emptyCharacteristicsWithoutValues = getCharacteristicsWithoutValues(resources, true)
   if (emptyCharacteristicsWithoutValues.length > 0) {
      return `Values must be specified for all empty characteristics. Empty characteristics without values: 
               ${emptyCharacteristicsWithoutValues.join(', ')}`
   }
   const emptyCharacteristicsWithIncorrectValues = getCharacteristicsWithIncorrectValues(resources, true)
   if (emptyCharacteristicsWithIncorrectValues.length > 0) {
      return `Values of resource empty characteristics must be greater than 0. Empty characteristics with incorrect values: 
               ${emptyCharacteristicsWithIncorrectValues.join(', ')}`
   }
   const emptyCharacteristicsWithMissingConverter = getCharacteristicsWithMissingConverters(resources, true)
   if (emptyCharacteristicsWithMissingConverter.length > 0) {
      return `Either none or both unit converters must be specified for each empty characteristic. Empty characteristics with missing converters: 
               ${emptyCharacteristicsWithMissingConverter.join(', ')}`
   }

   return ''
}
