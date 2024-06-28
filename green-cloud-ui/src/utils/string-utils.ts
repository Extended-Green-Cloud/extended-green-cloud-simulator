import { capitalize } from 'lodash'

/**
 * Method splits the enum value that separates words with '_' and capitalizes the first word.
 *
 * @param {string}enumVal enum value
 * @returns capitalized and split enum value
 */
const capitalizeEnum = (enumVal: string) => {
   return capitalize(enumVal.replaceAll('_', ' ').toLowerCase())
}

export { capitalizeEnum }
