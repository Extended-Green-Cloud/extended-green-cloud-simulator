import { useEffect, useState } from 'react'

import { AgentEvent, EventState, PowerShortageEventData } from '@types'
import { toast } from 'react-toastify'
import { Button } from 'components/common'

interface Props {
   event: AgentEvent
   label: string
   agentName: string
   triggerPowerShortage: (data: PowerShortageEventData) => void
}
const buttonWaitLabel = 'Wait before next event triggering'
/**
 * Component represents fields comnnected with the trigger power shortage event for given agent
 *
 * @param {AgentEvent}[event] - power shortage event
 * @param {string}[label] - label describing event card
 * @param {string}[agentName] - name of the agent affected by power shortage
 * @param {func}[triggerPowerShortage] - action responsible for power shortage event
 *
 * @returns JSX Element
 */
const PowerShortageEvent = ({ event, label, agentName, triggerPowerShortage }: Props) => {
   const [inputVal, setInputVal] = useState<number>(0)
   const buttonLabel = event.disabled ? buttonWaitLabel : label

   useEffect(() => {
      setInputVal(0)
   }, [agentName])

   const getButtonStyle = () => {
      const eventStyle = event.state === EventState.ACTIVE ? 'event-active-button' : 'event-inactive-button'
      return ['event-button', eventStyle].join(' ')
   }

   function handlePowerShortageTrigger() {
      if (typeof inputVal === 'undefined' && event?.state === EventState.ACTIVE) {
         toast.dismiss()
         toast.info('The new maximum capacity must be specified!')
      } else {
         const message = event?.state === EventState.ACTIVE ? 'triggered' : 'finished'
         toast.dismiss()
         toast.warn(`Power shortage ${message} in ${agentName}`)
         triggerPowerShortage({
            agentName,
            newMaximumCapacity: inputVal as number
         })
         setInputVal(0)
      }
   }

   return (
      <>
         <Button
            {...{
               buttonClassName: getButtonStyle(),
               onClick: handlePowerShortageTrigger,
               isDisabled: event.disabled,
               title: buttonLabel.toUpperCase()
            }}
         />
      </>
   )
}

export default PowerShortageEvent
