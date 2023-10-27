import { SwitchOnOffEvent, SwitchOnOffEventData } from '@types'
import { toast } from 'react-toastify'
import { Button } from 'components/common'

interface Props {
   event: SwitchOnOffEvent
   label: string
   agentName: string
   switchServerState: (data: SwitchOnOffEventData) => void
}
const buttonWaitLabel = "Wait until server's state will fully change"

/**
 * Component represents field that switches off or on the selected server
 *
 * @param {AgentEvent}[event] - switch on/off event
 * @param {string}[label] - label describing event card
 * @param {string}[agentName] - name of the agent affected by weather drop
 * @param {func}[switchServerState] - action responsible for weather drop event
 *
 * @returns JSX Element
 */
const SwitchOnOffCard = ({ event, label, agentName, switchServerState }: Props) => {
   const buttonLabel = event.disabled ? buttonWaitLabel : label
   const isSwitchingOff = event.isServerOn ? 'off' : 'on'

   const buttonStyle = ['event-button', 'event-active-button'].join(' ')

   function handleWeatherDropTrigger() {
      toast.dismiss()
      toast.warn(`Switching server ${agentName} ${isSwitchingOff}!`)
      switchServerState({ agentName })
   }

   return (
      <>
         <Button
            {...{
               buttonClassName: buttonStyle,
               onClick: handleWeatherDropTrigger,
               isDisabled: event.disabled,
               title: buttonLabel.toUpperCase()
            }}
         />
      </>
   )
}

export default SwitchOnOffCard
