import { styles } from './event-panel-styles'
import SubtitleContainer from '../../common/subtitle-container/subtitle-container'

import DetailsField from 'components/common/details-field/details-field'
import EventContainer from './event-container/event-container'
import { Agent, PowerShortageEventData } from '@types'

const description = 'Click on an agent to display available event actions'
const noEventsText = 'Agent does not have any available event actions'

interface Props {
   selectedAgent?: Agent
   triggerPowerShortage: (data: PowerShortageEventData) => void
}

/**
 * Component represents panel that can be used by the administrators to handle events condcuted on cloud network agents
 *
 * @returns JSX Element
 */
export const EventPanel = ({ selectedAgent, triggerPowerShortage }: Props) => {
   const eventContainerStyle = {
      ...styles.singleEventParentContainer,
      justifyContent:
         selectedAgent && selectedAgent?.events.length !== 0
            ? 'center'
            : undefined,
   }

   const mapToEventFields = () => {
      return selectedAgent?.events.map((event) => {
         const key = [selectedAgent.name, event.type].join('_')
         return (
            <EventContainer
               {...{ selectedAgent, event, key, triggerPowerShortage }}
            />
         )
      })
   }

   const generateEventTypes = () => {
      if (!selectedAgent) {
         return <SubtitleContainer text={description} />
      } else if (selectedAgent?.events.length === 0) {
         return <SubtitleContainer text={noEventsText} />
      } else {
         return mapToEventFields()
      }
   }

   return (
      <div>
         {selectedAgent && (
            <DetailsField {...{ label: selectedAgent?.name, isHeader: true }} />
         )}
         <div style={eventContainerStyle}>{generateEventTypes()}</div>
      </div>
   )
}
