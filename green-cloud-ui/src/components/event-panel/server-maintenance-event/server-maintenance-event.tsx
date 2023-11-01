import React, { useState } from 'react'
import { ServerMaintenanceEventData, ResourceMap, ServerMaintenanceEvent } from '@types'
import { Button } from 'components/common'
import ServerMaintenanceModal from './server-maintenance-modal/server-maintenance-modal'
interface Props {
   event: ServerMaintenanceEvent
   label: string
   currentResources: ResourceMap
   agentName: string
   isAvailable: boolean
   triggerServerMaintenance: (data: ServerMaintenanceEventData) => void
   resetServerMaintenance: (agentName: string) => void
}

/**
 * Component represents container that allows to perform server's maintenance
 *
 * @param {AgentEvent}[event] - server maintenance event
 * @param {string}[label] - label describing event card
 * @param {boolean}[isAvailable] - flag indicating if event is available
 * @param {ResourceMap}[currentResources] - current resources of the server
 * @param {string}[agentName] - name of the server for which maintenance is being performed
 * @param {func}[triggerPowerShortage] - action responsible for triggering changes in server resource configuration
 * @param {func}[resetServerMaintenance] - action responsible for resetting maintenance process
 *
 * @returns JSX Element
 */
const ServerMaintenanceCard = ({
   event,
   label,
   isAvailable,
   agentName,
   currentResources,
   triggerServerMaintenance,
   resetServerMaintenance
}: Props) => {
   const [isOpen, setIsOpen] = useState<boolean>(false)
   const buttonStyle = ['event-button', 'event-active-button'].join(' ')

   const handleServerMaintenanceTrigger = (newResources: ResourceMap) => {
      triggerServerMaintenance({ agentName, newResources })
   }

   const handleServerMaintenanceReset = () => {
      resetServerMaintenance(agentName)
      setIsOpen(false)
   }

   return (
      <>
         <ServerMaintenanceModal
            {...{
               isOpen,
               setIsOpen,
               currentResources,
               event,
               isAvailable,
               resetServerMaintenance: handleServerMaintenanceReset,
               handleServerMaintenanceTrigger
            }}
         />
         <Button
            {...{
               buttonClassName: buttonStyle,
               onClick: () => setIsOpen(!isOpen),
               title: label.toUpperCase()
            }}
         />
      </>
   )
}

export default ServerMaintenanceCard
