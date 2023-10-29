import { Button } from 'components/common'
import { styles } from './resource-configuration-title-styles'
import { UpdateResource } from '../resource-configuration/resource-configuration'
import { IconCross } from '@assets'

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
   const { wrapper, text } = styles

   const deleteResource = () => {
      setNewResources((prevState) => {
         const newResources = { ...prevState }
         delete newResources[resourceName]
         return { ...newResources }
      })
   }

   return (
      <div style={wrapper}>
         <Button
            {...{
               title: <IconCross size="22px" color="var(--gray-3)" />,
               onClick: () => deleteResource(),
               buttonClassName: ''
            }}
         />
         <div style={text}>{resourceName}</div>
      </div>
   )
}

export default ResourceTitle
