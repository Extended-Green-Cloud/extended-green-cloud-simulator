import { EVENT_MAP } from './event-container-config'
import { styles } from './event-container-styles'

import {
   Agent,
   AgentEvent,
   EventType,
   PowerShortageEvent,
   PowerShortageEventData,
   PowerShortageEventState,
   ServerAgent,
   ServerMaintenanceEvent,
   ServerMaintenanceEventData,
   SwitchOnOffEvent,
   SwitchOnOffEventData,
   WeatherDropEventData
} from '@types'
import PowerShortageCard from 'components/event-panel/power-shortage-event/power-shortage-event'
import Collapse from 'components/common/collapse/collapse'
import WeatherDropCard from '../weather-drop-event/weather-drop-event'
import SwitchOnOffCard from '../switch-off-on-event/switch-off-on-event'
import ServerMaintenanceCard from '../server-maintenance-event/server-maintenance-event'
import { getEventByType } from '@utils'

interface Props {
   selectedAgent: Agent
   event: AgentEvent
   triggerPowerShortage: (data: PowerShortageEventData) => void
   triggerWeatherDrop: (data: WeatherDropEventData) => void
   switchServerState: (data: SwitchOnOffEventData) => void
   triggerServerMaintenance: (data: ServerMaintenanceEventData) => void
   resetServerMaintenance: (agentName: string) => void
}

/**
 * Component represents singualr generic event container
 *
 * @param {Agent}[selectedAgent] - agent for which the event is being generated
 * @param {AgentEvent}[event] - generated event
 * @param {func}[triggerPowerShortage] - action responsible for power shortage event
 * @param {func}[triggerWeatherDrop] - action responsible for weather drop event
 * @returns JSX Element
 */
const EventContainer = ({
   selectedAgent,
   event,
   triggerPowerShortage,
   triggerWeatherDrop,
   switchServerState,
   triggerServerMaintenance,
   resetServerMaintenance
}: Props) => {
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
         const agentName = selectedAgent?.name
         switch (event.type) {
            case EventType.POWER_SHORTAGE_EVENT: {
               const powerShortageEvent = event as PowerShortageEvent
               const label = eventEntry.labels[powerShortageEvent.state].toUpperCase()
               return <PowerShortageCard {...{ event: powerShortageEvent, label, agentName, triggerPowerShortage }} />
            }
            case EventType.WEATHER_DROP_EVENT: {
               const label = eventEntry.label
               return <WeatherDropCard {...{ event, label, agentName, triggerWeatherDrop }} />
            }
            case EventType.SWITCH_ON_OFF_EVENT: {
               const switchServerStateEvent = event as SwitchOnOffEvent
               const onOffState = switchServerStateEvent.isServerOn ? 'OFF' : 'ON'
               const label = eventEntry.labels[onOffState].toUpperCase()
               return <SwitchOnOffCard {...{ event: switchServerStateEvent, label, agentName, switchServerState }} />
            }
            case EventType.SERVER_MAINTENANCE_EVENT: {
               const switchServerStateEvent = getEventByType(
                  EventType.SWITCH_ON_OFF_EVENT,
                  selectedAgent
               ) as SwitchOnOffEvent
               const powerShortageEvent = getEventByType(
                  EventType.POWER_SHORTAGE_EVENT,
                  selectedAgent
               ) as PowerShortageEvent

               const isAvailable =
                  powerShortageEvent.state === PowerShortageEventState.ACTIVE && !switchServerStateEvent.isServerOn
               const maintenanceEvent = event as ServerMaintenanceEvent
               const label = eventEntry.label.toUpperCase()
               const currentResources = (selectedAgent as ServerAgent).resources
               return (
                  <ServerMaintenanceCard
                     {...{
                        event: maintenanceEvent,
                        currentResources,
                        label,
                        agentName,
                        isAvailable,
                        triggerServerMaintenance,
                        resetServerMaintenance
                     }}
                  />
               )
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
