import { AddWithInput } from 'components/common'
import { Resource, ResourceMap } from '@types'
import { UpdateResource } from '../resource-form'

interface Props {
   resources: ResourceMap
   setNewResources: UpdateResource
   skipEmptyResource?: boolean
}

const getNewResource = (): Resource => ({
   characteristics: {},
   emptyResource: {
      characteristics: {},
      emptyResource: null,
      sufficiencyValidator: '',
      resourceComparator: ''
   },
   sufficiencyValidator: '',
   resourceComparator: ''
})

const getNewResourceWithoutEmptyResource = (): Resource => ({
   characteristics: {},
   emptyResource: null,
   sufficiencyValidator: '',
   resourceComparator: ''
})

/**
 * Component representing form used to add new resource
 *
 * @param {ResourceMap}[resources] - resource map
 * @param {UpdateResource}[setNewResources] - function used to update resource map
 * @param {boolean}[skipEmptyResource] - parameter specifying if the empty resource component should be skipped
 *
 * @returns JSX Element
 */
const AddNewResource = ({ resources, setNewResources, skipEmptyResource }: Props) => {
   const addEmptyResource = (inputName: string) => {
      setNewResources((prevState) => {
         const emptyResource = skipEmptyResource ? getNewResourceWithoutEmptyResource() : getNewResource()
         return {
            ...prevState,
            [inputName]: emptyResource
         }
      })
   }

   return (
      <AddWithInput
         {...{
            handleButtonPress: addEmptyResource,
            inputTitle: 'Provide new resource name',
            buttonTitle: 'Add new resource',
            isDisabled: (name: string) => Object.keys(resources).includes(name)
         }}
      />
   )
}

export default AddNewResource
