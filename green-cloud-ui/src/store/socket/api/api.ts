import { END, eventChannel } from "redux-saga"
import { cloudNetworkActions } from "../../cloud-network/actions"
import { socketActions } from "../actions"
import { MessagePayload, MessageType } from '@types'
import { NotUndefined } from "@redux-saga/types"

type Emitter = (input: NotUndefined | END) => void

interface EventHandlerProps {
    event: any,
    emitter: Emitter
}

interface MessageHandlerProps {
    msg: MessagePayload,
    emitter: Emitter
}

export function establishSocketConnection() {
    return eventChannel(emitter => {
        const ws = new WebSocket(process.env.REACT_APP_WEB_SOCKET_URL)

        ws.onopen = () => handleSocketOpen(emitter)
        ws.onmessage = (e) => handleSocketMessage({ event: e, emitter })
        ws.onerror = (e) => handleSocketError(e)
        ws.onclose = () => handleSocketClose()

        return () => ws.close()
    })
}

const handleSocketOpen = (emitter: Emitter) => {
    console.log("Connection was opened")
    emitter(socketActions.connectSocket())
}

const handleSocketClose = () => {
    console.log("Connection was closed. Trying to reconnect")
    setTimeout(() => { establishSocketConnection() }, 4000);
}

const handleSocketError = (error: Event) => {
    console.log('WebSocket error ' + error)
    console.dir(error)
}

const handleSocketMessage = ({ event, emitter }: EventHandlerProps) => {
    try {
        const msg = JSON.parse(event.data)
        const test = msg as MessagePayload
        console.log(test)
        dispatchActionOnMessage({ msg: test, emitter })
    } catch (e: any) {
        console.error(`Error parsing : ${e.data}`)
    }
}

const dispatchActionOnMessage = ({ msg, emitter }: MessageHandlerProps) => {
    switch (msg.type) {
        case MessageType.INCREMENT_FAILED_JOBS:
            emitter(cloudNetworkActions.incrementFailedJobs())
            break
        case MessageType.INCREMENT_FINISHED_JOBS:
            emitter(cloudNetworkActions.incrementFinishedJobs())
            break
        case MessageType.REGISTER_AGENT:
            emitter(cloudNetworkActions.registerAgent(msg))
            break
        case MessageType.SET_CLIENT_JOB_STATUS:
            emitter(cloudNetworkActions.setClientJobStatus(msg))
            break
        case MessageType.SET_CLIENT_NUMBER:
            emitter(cloudNetworkActions.setClientNumber(msg))
            break
        case MessageType.SET_IS_ACTIVE:
            emitter(cloudNetworkActions.setIsActive(msg))
            break
        case MessageType.SET_JOBS_COUNT:
            emitter(cloudNetworkActions.setJobsCount(msg))
            break
        case MessageType.SET_MAXIMUM_CAPACITY:
            emitter(cloudNetworkActions.setMaximumCapacity(msg))
            break
        case MessageType.SET_ON_HOLD_JOBS_COUNT:
            emitter(cloudNetworkActions.setOnHoldJobsCount(msg))
            break
        case MessageType.SET_SERVER_BACK_UP_TRAFFIC:
            emitter(cloudNetworkActions.setServerBackUpTraffic(msg))
            break
        case MessageType.SET_TRAFFIC:
            emitter(cloudNetworkActions.setTraffic(msg))
            break
        case MessageType.UPDATE_CURRENT_ACTIVE_JOBS:
            emitter(cloudNetworkActions.updateCurrentActiveJobsNumber(msg))
            break
        case MessageType.UPDATE_CURRENT_CLIENTS:
            emitter(cloudNetworkActions.updateCurrentClientNumber(msg))
            break
        case MessageType.UPDATE_CURRENT_PLANNED_JOBS:
            emitter(cloudNetworkActions.updateCurrentPlannedJobsNumber(msg))
            break
        case MessageType.UPDATE_TOTAL_PRICE:
            emitter(cloudNetworkActions.setTotalPrice(msg))
            break
        case MessageType.DISPLAY_MESSAGE_ARROW:
            emitter(cloudNetworkActions.displayAgentEdge(msg))
            setTimeout(() => emitter(cloudNetworkActions.hideAgentEdge(msg)), 2000)
            break
    }
}