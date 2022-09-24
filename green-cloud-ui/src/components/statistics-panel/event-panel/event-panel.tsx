import Card from "components/card/card"
import { EVENT_MAP } from "./event-panel-config"
import { styles } from "./event-panel-styles"
import './css/event-panel-button-styles.css'
import NumericInput from "../numeric-input/numeric-input"
import SubtitleContainer from "../subtitle-container/subtitle-container"
import { toast } from 'react-toastify';

import { useAppDispatch, getEventByType, agentsActions, useAppSelector } from "@store"
import { AgentEvent, EventState, EventType, PowerShortageEvent } from "@types"

const header = "Agent event panel"
const description = "Click on an agent to display available event actions"
const noEventsText = "Agent does not have any available event actions"
const placeholder = 'Provide maximum capacity'
const buttonWaitLabel = 'Wait before next event triggering'
const topButtonLabel = "Maximum Capacity"

/**
 * Component represents panel that can be used by the administrators to handle events condcuted on cloud network agents
 * @returns JSX Element
 */
const EventPanel = () => {
    const agentState = useAppSelector(state => state.agents)
    const selectedAgent = agentState.agents.find(agent => agent.name === agentState.selectedAgent)
    const dispatch = useAppDispatch()

    function handlePowerShortageTrigger(e: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
        if (selectedAgent) {
            const { name } = selectedAgent
            const event = getEventByType(selectedAgent.events, EventType.POWER_SHORTAGE_EVENT)

            if (!event?.data?.newMaximumCapacity && event?.state === EventState.ACTIVE) {
                toast.info("The new maximum capacity must be specified!")
            } else {
                const message = event?.state === EventState.ACTIVE ? 'triggered' : 'finished'
                toast.warn(`Power shortage ${message} in ${selectedAgent.name}`)
                dispatch(agentsActions.triggerPowerShortage(name))
                setTimeout(() => dispatch(agentsActions.unlockPowerShortageEvent(name)), 2000)
            }
        }
    }

    function handlePowerShortageChange(e: React.ChangeEvent<HTMLInputElement>) {
        const newMaximumCapacity = parseFloat(e.target.value)
        const agentName = selectedAgent?.name

        dispatch(agentsActions.setPowerShortageCapacity({ agentName, newMaximumCapacity }))
    }


    const getEventFields = (event: AgentEvent, eventEntry: any) => {
        switch (event.type) {
            case EventType.POWER_SHORTAGE_EVENT: {
                const label = eventEntry.labels[event.state].toUpperCase()
                return getPowerShortageFields(event, label)
            }
        }
    }

    const getPowerShortageFields = (event: AgentEvent, label: string) => {
        const powerShortage = event.data as PowerShortageEvent
        const buttonStyle =
            ['button',
                event.state === EventState.ACTIVE ?
                    'active-button' :
                    'inactive-button'
            ].join(' ')
        const disabled = event.state === EventState.INACTIVE || event.disabled
        const buttonLabel = event.disabled ? buttonWaitLabel : label
        return (
            <>
                <NumericInput {...{
                    value: powerShortage?.newMaximumCapacity,
                    handleChange: handlePowerShortageChange,
                    label: topButtonLabel,
                    disabled,
                    placeholder
                }} />
                <button className={buttonStyle} onClick={handlePowerShortageTrigger} disabled={event.disabled}>
                    {buttonLabel.toUpperCase()}
                </button>
            </>
        )
    }

    const mapToEventFields = () => {
        return (selectedAgent?.events.map(event => {
            const eventEntry = { ...EVENT_MAP as any }[event.type]
            const fields = getEventFields(event, eventEntry)
            return (
                <div style={styles.singleEventContainer}>
                    <span style={styles.eventTitle}>{eventEntry.title.toUpperCase()}</span>
                    {fields}
                </div>
            )
        }))
    }

    const generateEventTypes = () => {
        if (!selectedAgent) {
            return (<SubtitleContainer text={description} />)
        } else if (selectedAgent?.events.length === 0) {
            return (<SubtitleContainer text={noEventsText} />)
        } else {
            return mapToEventFields()
        }
    }

    return (
        <Card {...{ header, containerStyle: styles.eventContainer }}>
            <div style={styles.singleEventParentContainer}>
                {/* {generateEventTypes()} */}
            </div>
        </Card>
    )
}

export default EventPanel