import { IconInfo } from '@assets'
import { PlacesType, Tooltip } from 'react-tooltip'
import 'react-tooltip/dist/react-tooltip.css'
import { styles } from './info-tooltip-styles'

interface Props {
   header: string
   id: string
   content?: React.ReactNode
   place?: PlacesType
}

const iconSize = '15px'

/**
 * Component represents an informative tooltip
 *
 * @param {string} header - header text
 * @param {string} id - identifier of the tooltip
 * @param {object} content - optional content of the tooltip
 * @param {PlacesType} place - optional tooltip place
 * @returns JSX Elment
 */
const InfoTooltip = ({ header, id, content, place }: Props) => {
   const { tooltipHeader, tooltipHeaderText, tooltipStyle } = styles

   const tooltipContent = (
      <div>
         <div style={tooltipHeader}>
            <IconInfo size={iconSize} />
            <span style={tooltipHeaderText}>{header}</span>
         </div>
         {content}
      </div>
   )

   return (
      <Tooltip
         anchorId={id}
         style={tooltipStyle}
         place={place ?? 'left'}
         delayShow={1000}
      >
         {tooltipContent}
      </Tooltip>
   )
}

export default InfoTooltip
