import React, { useState } from 'react'
import { ResourceMap, ServerMaintenanceEvent } from '@types'
import { Button, Modal, ResourceForm } from 'components/common'
import { IconExclamation, iconCheckMark, iconCross } from '@assets'
import { validateResources } from '@utils'
import { styles } from './server-maintenance-modal-styles'

interface Props {
   event: ServerMaintenanceEvent
   isAvailable: boolean
   isOpen: boolean
   setIsOpen: (open: boolean) => void
   currentResources: ResourceMap
   resetServerMaintenance: () => void
   handleServerMaintenanceTrigger: (resources: ResourceMap) => void
}

type MaintenanceStatusLabels = {
   [key in keyof ServerMaintenanceEvent]: {
      [key in 'true' | 'false']: string
   }
}

const MAINTENANCE_STATES: (keyof ServerMaintenanceEvent)[] = [
   'sendNewData',
   'processDataInServer',
   'informationInManager',
   'maintenanceCompleted'
]

const LABELS_MAP = {
   sendNewData: {
      true: 'Server resource configuration was successfully sent to the system.',
      false: 'An error occurred while passing server resource configuration.'
   },
   processDataInServer: {
      true: 'Server resource configuration was successfully processed by server agent.',
      false: 'An error occurred while processing server resource configuration by server agent.'
   },
   informationInManager: {
      true: 'CNA received information about new server configuration.',
      false: 'An error occurred while sending server resource configuration to the CNA.'
   },
   maintenanceCompleted: {
      true: 'Server maintenance was successfully completed. Server can now be started.',
      false: 'An error occurred on the last step of server maintenance.'
   }
} as MaintenanceStatusLabels

/**
 * Component represents modal allowing to configure server resources
 *
 * @param {AgentEvent}[event] - server maintenance event
 * @param {boolean}[isAvailable] - flag indicates if maintenance event can be triggered
 * @param {boolean}[isOpen] - flag indicating if modal is open
 * @param {func}[setIsOpen] - function closing/opening the modal
 * @param {ResourceMap}[currentResources] - current resources of the server
 * @param {func}[resetServerMaintenance] - action responsible for resetting maintenance process
 * @param {func}[handleServerMaintenanceTrigger] - action responsible for triggering changes in server resource configuration
 *
 * @returns JSX Element
 */
const ServerMaintenanceModal = ({
   isOpen,
   setIsOpen,
   currentResources,
   event,
   isAvailable,
   resetServerMaintenance,
   handleServerMaintenanceTrigger
}: Props) => {
   const [newResources, setNewResources] = useState<ResourceMap>(currentResources)
   const [resetResource, setResetResource] = useState<boolean>(false)
   const [errorText, setErrorText] = useState<string>('')
   const isMaintenanceFinished = event.maintenanceCompleted !== null && event.hasStarted
   const isMaintenanceOngoing = event.maintenanceCompleted === null && event.hasStarted && !event.hasError

   const { errorWrapper, contentWrapper, modal, errorTextStyle, statusWrapper, iconStyle } = styles
   const buttonResetStyle = ['medium-light-gray-button-active', 'medium-light-gray-button', 'full-width-button'].join(
      ' '
   )
   const buttonApplyChangesStyle = ['event-active-button', 'event-button'].join(' ')
   const buttonText =
      !isMaintenanceOngoing && isMaintenanceFinished
         ? 'Finish server maintenance'
         : isMaintenanceOngoing
         ? 'Wait till maintenance completes'
         : 'Save changes and send to server'

   const resetConfiguration = () => {
      if (isMaintenanceFinished) {
         setNewResources(currentResources)
         setResetResource(true)
         setErrorText('')
      }
   }

   const resetMaintenance = () => {
      resetServerMaintenance()
   }

   const submitForm = () => {
      setErrorText(validateResources(newResources))
      console.log(errorText)

      if (errorText === '') {
         handleServerMaintenanceTrigger(newResources)
      }
   }

   const getMaintenanceResults = () => {
      return (
         <div>
            {MAINTENANCE_STATES.map((state) => {
               if (event[state] !== null) {
                  const hasSucceeded = event[state]
                  const message = LABELS_MAP[state][hasSucceeded ? 'true' : 'false']
                  const color = hasSucceeded ? 'var(--green-1)' : 'var(--red-1)'
                  const icon = hasSucceeded ? iconCheckMark : iconCross
                  return (
                     <div style={{ ...statusWrapper, color }}>
                        <img style={iconStyle} src={icon} alt="Maintenance step success icon" />
                        {`${message}...`}
                     </div>
                  )
               }
            })}
         </div>
      )
   }

   return (
      <>
         <Modal {...{ isOpen, setIsOpen, header: 'Server configuration'.toUpperCase(), contentStyle: modal }}>
            <div style={contentWrapper}>
               <div>
                  <ResourceForm {...{ newResources, setNewResources, resetResource, setResetResource }} />
               </div>
               <div>
                  {getMaintenanceResults()}
                  {errorText !== '' && (
                     <div style={errorWrapper}>
                        <IconExclamation {...{ size: '1.5em', color: 'var(--red-1)' }} />
                        <div style={errorTextStyle}>{`Invalid configuration! ${errorText}`}</div>
                     </div>
                  )}
                  <Button
                     {...{
                        buttonClassName: buttonResetStyle,
                        isDisabled: isMaintenanceOngoing,
                        onClick: () => resetConfiguration(),
                        title: 'Reset changes'.toUpperCase()
                     }}
                  />
                  <Button
                     {...{
                        buttonClassName: buttonApplyChangesStyle,
                        isDisabled: !isAvailable || isMaintenanceOngoing,
                        onClick: () => (isMaintenanceFinished ? resetMaintenance() : submitForm()),
                        title: buttonText.toUpperCase()
                     }}
                  />
               </div>
            </div>
         </Modal>
      </>
   )
}

export default ServerMaintenanceModal
