import Card from "components/card/card"
import { EVENT_MAP } from "./event-panel-config"
import { styles } from "./event-panel-styles"
import SubtitleContainer from "../subtitle-container/subtitle-container"

import { useAppSelector } from "@store"
import { AgentEvent, EventType } from "@types"
import PowerShortageCard from "components/power-shortage-card/power-shortage-card"

const header = "Agent event panel"
const description = "Click on an agent to display available event actions"
const noEventsText = "Agent does not have any available event actions"

/**
 * Component represents panel that can be used by the administrators to handle events condcuted on cloud network agents
 * 
 * @returns JSX Element
 */
const EventPanel = () => {
    const agentState = useAppSelector(state => state.agents)
    const selectedAgent = agentState.agents.find(agent => agent.name === agentState.selectedAgent)
    const eventContainerStyle = { ...styles.singleEventParentContainer, justifyContent: selectedAgent && selectedAgent?.events.length !== 0 ? 'center' : undefined }

    const getEventFields = (event: AgentEvent, eventEntry: any) => {
        if (selectedAgent) {
            switch (event.type) {
                case EventType.POWER_SHORTAGE_EVENT: {
                    const label = eventEntry.labels[event.state].toUpperCase()
                    return <PowerShortageCard {...{ event, label, agentName: selectedAgent?.name }} />
                }
            }
        }
    }

    const mapToEventFields = () => {
        return (selectedAgent?.events.map(event => {
            const eventEntry = { ...EVENT_MAP as any }[event.type]
            const fields = getEventFields(event, eventEntry)
            return (
                <div style={styles.singleEventContainer}>
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
            <div style={eventContainerStyle}>
                {generateEventTypes()}
            </div>
        </Card>
    )
}

export default EventPanel