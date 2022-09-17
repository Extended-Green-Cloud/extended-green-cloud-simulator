import { SocketEvent } from "@types"
import { eventChannel } from "redux-saga"
import { cloudNetworkActions } from "../../cloud-network/actions"
import { socketActions } from "../actions"


export function establishSocketConnection() {
    return eventChannel(emitter => {
        const ws = new WebSocket(process.env.REACT_APP_WEB_SOCKET_URL)

        ws.onopen = () => {
            console.log("Connection was opened")
            emitter(socketActions.connectSocket())
        }
        ws.onmessage = (e) => handleIncomingMessage({ event: e, emitter })
        ws.onerror = (error) => {
            console.log('WebSocket error ' + error)
            console.dir(error)
        }
        ws.onclose = () => {
            console.log("Connection was closed")
            setTimeout(() => { establishSocketConnection() }, 4000);
        }

        return () => {
            console.log('Connection closed')
            ws.close()
        }
    })
}

const handleIncomingMessage = ({ event, emitter }: SocketEvent) => {
    let msg = null
    try {
        msg = JSON.parse(event.data)
    } catch (e: any) {
        console.error(`Error parsing : ${e.data}`)
    }
    if (msg) {
        if (msg.type === 'traffic') {
            return emitter(cloudNetworkActions.setAgentTraffic(msg))
        }
        return emitter(cloudNetworkActions.incrementPrice())
    }
}