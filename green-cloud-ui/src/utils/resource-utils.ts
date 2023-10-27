import {
   MultiLevelDetails,
   MultiLevelSubEntries,
   MultiLevelValues,
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

   return `${inUseResourceCVal} ${inUseResourceC?.unit ?? resourceC.unit} \\ ${resourceCVal} ${resourceC.unit}`
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
