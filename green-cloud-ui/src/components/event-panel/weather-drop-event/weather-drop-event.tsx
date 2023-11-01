import { WeatherDropEventData } from '@types'
import { toast } from 'react-toastify'
import { Button } from 'components/common'
import { CommonAgentEvent } from 'types/event/agent-event/common-agent-event'

interface Props {
   event: CommonAgentEvent
   label: string
   agentName: string
   triggerWeatherDrop: (data: WeatherDropEventData) => void
}
const buttonWaitLabel = 'Wait before next event triggering'
/**
 * Component represents fields connected with the trigger of weather drop event for given agent
 *
 * @param {AgentEvent}[event] - weather drop event
 * @param {string}[label] - label describing event card
 * @param {string}[agentName] - name of the agent affected by weather drop
 * @param {func}[triggerWeatherDrop] - action responsible for weather drop event
 *
 * @returns JSX Element
 */
const WeatherDropCard = ({ event, label, agentName, triggerWeatherDrop }: Props) => {
   const buttonLabel = event.disabled ? buttonWaitLabel : label

   const buttonStyle = ['event-button', 'event-active-button'].join(' ')

   function handleWeatherDropTrigger() {
      toast.dismiss()
      toast.warn(`ALERT! Weather conditions of Green Sources being under ${agentName} will worsen in 5s!`)
      triggerWeatherDrop({
         agentName,
         duration: 30
      })
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

export default WeatherDropCard
