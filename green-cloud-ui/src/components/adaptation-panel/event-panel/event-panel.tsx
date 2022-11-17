import { styles } from './event-panel-styles'
import SubtitleContainer from '../../common/subtitle-container/subtitle-container'

import { useAppSelector } from '@store'
import DetailsField from 'components/common/details-field/details-field'
import EventContainer from './event-container/event-container'

const description = 'Click on an agent to display available event actions'
const noEventsText = 'Agent does not have any available event actions'

/**
 * Component represents panel that can be used by the administrators to handle events condcuted on cloud network agents
 *
 * @returns JSX Element
 */
const EventPanel = () => {
   const agentState = useAppSelector((state) => state.agents)
   const selectedAgent = agentState.agents.find(
      (agent) => agent.name === agentState.selectedAgent
   )
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
         return <EventContainer {...{ selectedAgent, event, key }} />
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

export default EventPanel
