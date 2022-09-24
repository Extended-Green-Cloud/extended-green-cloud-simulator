import { END, eventChannel } from "redux-saga"
import { cloudNetworkActions } from "../../cloud-network/actions"
import { socketActions } from "../actions"
import { MessagePayload, MessageType } from '@types'
import { NotUndefined } from "@redux-saga/types"
import { agentsActions } from "store/agent"
import { modifyEdgeState } from "@utils"

type Emitter = (input: NotUndefined | END) => void

interface EventHandlerProps {
    event: any,
    emitter: Emitter
}

interface MessageHandlerProps {
    msg: MessagePayload,
    emitter: Emitter
}

export function establishSocketConnection(ws: WebSocket, address: string) {
    return eventChannel(emitter => {
        ws = new WebSocket(address)

        ws.onopen = () => handleSocketOpen(emitter, ws)
        ws.onmessage = (e) => handleSocketMessage({ event: e, emitter })
        ws.onerror = (e) => handleSocketError(e)
        ws.onclose = () => handleSocketClose(ws, address)

        return () => ws.close()
    })
}


const handleSocketOpen = (emitter: Emitter, ws: WebSocket) => {
    console.log("Connection was opened")
    emitter(socketActions.connectSocket())
}

const handleSocketClose = (ws: WebSocket, address: string) => {
    console.log("Connection was closed. Trying to reconnect")
    setTimeout(() => { establishSocketConnection(ws, address) }, 4000);
}

const handleSocketError = (error: Event) => {
    console.log('WebSocket error ' + error)
    console.dir(error)
}

const handleSocketMessage = ({ event, emitter }: EventHandlerProps) => {
    try {
        const msg = JSON.parse(event.data)
        const data = msg as MessagePayload
        console.debug(data)
        dispatchActionOnMessage({ msg: data, emitter })
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
            emitter(agentsActions.registerAgent(msg))
            break
        case MessageType.SET_CLIENT_JOB_STATUS:
            emitter(agentsActions.setClientJobStatus(msg))
            break
        case MessageType.SET_CLIENT_NUMBER:
            emitter(agentsActions.setClientNumber(msg))
            break
        case MessageType.SET_IS_ACTIVE:
            emitter(agentsActions.setIsActive(msg))
            break
        case MessageType.SET_JOBS_COUNT:
            emitter(agentsActions.setJobsCount(msg))
            break
        case MessageType.SET_MAXIMUM_CAPACITY:
            emitter(agentsActions.setMaximumCapacity(msg))
            break
        case MessageType.SET_ON_HOLD_JOBS_COUNT:
            emitter(agentsActions.setOnHoldJobsCount(msg))
            break
        case MessageType.SET_SERVER_BACK_UP_TRAFFIC:
            emitter(agentsActions.setServerBackUpTraffic(msg))
            break
        case MessageType.SET_TRAFFIC:
            emitter(agentsActions.setTraffic(msg))
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
            modifyEdgeState(msg, 'active')
            break
        case MessageType.HIDE_MESSAGE_ARROW:
            modifyEdgeState(msg, 'inactive')
            break
    }
}