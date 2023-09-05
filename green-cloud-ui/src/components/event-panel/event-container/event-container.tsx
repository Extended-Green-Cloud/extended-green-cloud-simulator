import { EVENT_MAP } from './event-container-config'
import { styles } from './event-container-styles'

import { Agent, AgentEvent, EventType, PowerShortageEventData } from '@types'
import PowerShortageCard from 'components/event-panel/power-shortage-event/power-shortage-event'
import Collapse from 'components/common/collapse/collapse'

interface Props {
   selectedAgent: Agent
   event: AgentEvent
   triggerPowerShortage: (data: PowerShortageEventData) => void
}

/**
 * Component represents singualr generic event container
 *
 * @param {Agent}[selectedAgent] - agent for which the event is being generated
 * @param {AgentEvent}[event] - generated event
 * @param {func}[triggerPowerShortage] - action responsible for power shortage event
 * @returns JSX Element
 */
const EventContainer = ({ selectedAgent, event, triggerPowerShortage }: Props) => {
   const {
      singleEventContainer,
      collapseWrapper,
      triggerWrapper,
      contentWrapper,
      triggerContainer,
      triggerDescription,
      triggerTitle
   } = styles
   const eventEntry = { ...(EVENT_MAP as any) }[event.type]

   const getEventFields = (eventEntry: any) => {
      if (selectedAgent) {
         switch (event.type) {
            case EventType.POWER_SHORTAGE_EVENT: {
               const label = eventEntry.labels[event.state].toUpperCase()
               const agentName = selectedAgent?.name
               return <PowerShortageCard {...{ event, label, agentName, triggerPowerShortage }} />
            }
         }
      }
   }

   const getEventHeader = () => (
      <div style={triggerContainer}>
         <div style={triggerTitle}>{eventEntry.title.toUpperCase()}</div>
         <div style={triggerDescription}>{eventEntry.description}</div>
      </div>
   )

   return (
      <div style={singleEventContainer}>
         <Collapse {...{ title: getEventHeader(), triggerStyle: triggerWrapper, wrapperStyle: collapseWrapper }}>
            <div style={contentWrapper}>{getEventFields(eventEntry)}</div>
         </Collapse>
      </div>
   )
}

export default EventContainer
