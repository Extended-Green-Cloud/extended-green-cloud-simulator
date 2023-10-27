import { PowerShortageEventState, PowerShortageEventData, PowerShortageEvent } from '@types'
import { toast } from 'react-toastify'
import { Button } from 'components/common'

interface Props {
   event: PowerShortageEvent
   label: string
   agentName: string
   triggerPowerShortage: (data: PowerShortageEventData) => void
}
const buttonWaitLabel = 'Wait before next event triggering'

/**
 * Component represents fields connected with the trigger power shortage event for given agent
 *
 * @param {AgentEvent}[event] - power shortage event
 * @param {string}[label] - label describing event card
 * @param {string}[agentName] - name of the agent affected by power shortage
 * @param {func}[triggerPowerShortage] - action responsible for power shortage event
 *
 * @returns JSX Element
 */
const PowerShortageCard = ({ event, label, agentName, triggerPowerShortage }: Props) => {
   const buttonLabel = event.disabled ? buttonWaitLabel : label
   const isPowerShortageActive = event.state === PowerShortageEventState.ACTIVE

   const getButtonStyle = () => {
      const eventStyle = isPowerShortageActive ? 'event-active-button' : 'event-inactive-button'
      return ['event-button', eventStyle].join(' ')
   }

   function handlePowerShortageTrigger() {
      const message = isPowerShortageActive ? 'triggered' : 'finished'
      toast.dismiss()
      toast.warn(`Power shortage ${message} in ${agentName}`)
      triggerPowerShortage({ agentName })
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

export default PowerShortageCard
