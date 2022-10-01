import { useState } from "react"
import './css/event-panel-button-styles.css'

import { agentsActions, useAppDispatch } from "@store"
import { AgentEvent, EventState } from "@types"
import { toast } from "react-toastify"
import NumericInput from "components/numeric-input/numeric-input"

interface Props {
    event: AgentEvent
    label: string,
    agentName: string
}


const placeholder = 'Provide maximum capacity'
const buttonWaitLabel = 'Wait before next event triggering'
const topButtonLabel = "Maximum Capacity"

/**
 * Component represents card used to trigger power shortage for given agent
 * 
 * @param {AgentEvent}[event] - power shortage event
 * @param {string}[label] - label describing event card
 * @param {string}[agentName] - name of the agent affected by power shortage 
 * 
 * @returns JSX Element
 */
const PowerShortageCard = ({ event, label, agentName }: Props) => {
    const [inputVal, setInputVal] = useState<number>()
    const dispatch = useAppDispatch()

    const getButtonStyle = () => {
        const eventStyle = event.state === EventState.ACTIVE ?
            'active-button' :
            'inactive-button'
        return ['button', eventStyle].join(' ')
    }
    const disabled = event.state === EventState.INACTIVE || event.disabled
    const buttonLabel = event.disabled ? buttonWaitLabel : label


    function handlePowerShortageChange(e: React.ChangeEvent<HTMLInputElement>) {
        setInputVal(parseFloat(e.target.value))
    }

    function handlePowerShortageTrigger(e: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
        if (!inputVal && event?.state === EventState.ACTIVE) {
            toast.dismiss()
            toast.info("The new maximum capacity must be specified!")
        } else {
            const message = event?.state === EventState.ACTIVE ? 'triggered' : 'finished'
            toast.dismiss()
            toast.warn(`Power shortage ${message} in ${agentName}`)
            dispatch(agentsActions.triggerPowerShortage({ agentName, newMaximumCapacity: inputVal as number }))
        }
    }

    return (
        <>
            <NumericInput {...{
                value: inputVal,
                handleChange: handlePowerShortageChange,
                label: topButtonLabel,
                disabled,
                placeholder
            }} />
            <button className={getButtonStyle()} onClick={handlePowerShortageTrigger} disabled={event.disabled}>
                {buttonLabel.toUpperCase()}
            </button>
        </>
    )
}

export default PowerShortageCard