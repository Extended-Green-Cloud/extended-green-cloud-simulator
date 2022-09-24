import { NotUndefined } from '@redux-saga/types';
import { EventChannel } from 'redux-saga';
import { all, call, ChannelTakeEffect, put, race, take } from 'redux-saga/effects';
import { socketActions } from '../actions';
import { establishSocketConnection } from '../api/api';

let wsNetwork = {
    ws: new WebSocket(process.env.REACT_APP_WEB_SOCKET_NETWORK_URL),
    address: process.env.REACT_APP_WEB_SOCKET_NETWORK_URL
}

let wsAgent = {
    ws: new WebSocket(process.env.REACT_APP_WEB_SOCKET_AGENT_URL),
    address: process.env.REACT_APP_WEB_SOCKET_AGENT_URL
}

export function* initializeSocket(ws: WebSocket, address: string): any {
    const channel: EventChannel<NotUndefined> = yield call(establishSocketConnection, ws, address)
    while (true) {
        const action: ChannelTakeEffect<{} | null> = yield take(channel)
        yield put(action)
    }
}

export function* keepSocketReconnecting(ws: WebSocket, address: string) {
    while (true) {
        yield take(socketActions.openSocketConnection);
        yield race({
            task: call(initializeSocket, ws, address),
            cancel: take(socketActions.closeSocketConnection),
        });
    }
}

export function sendMessageUsnigSocket(data: string) {
    if (wsNetwork.ws.readyState === WebSocket.OPEN && wsNetwork.ws) {
        wsNetwork.ws.send(data)
    }
}

export default function* socketSagas() {
    yield all([
        keepSocketReconnecting(wsAgent.ws, wsAgent.address),
        keepSocketReconnecting(wsNetwork.ws, wsNetwork.address)])
}