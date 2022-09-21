import { NotUndefined } from '@redux-saga/types';
import { EventChannel } from 'redux-saga';
import { all, call, ChannelTakeEffect, put, race, take } from 'redux-saga/effects';
import { socketActions } from '../actions';
import { establishSocketConnection } from '../api/api';

export function* initializeSocket(): any {
    const channel: EventChannel<NotUndefined> = yield call(establishSocketConnection)
    while (true) {
        const action: ChannelTakeEffect<{} | null>= yield take(channel)
        yield put(action)
    }
}

export function * keepSocketReconnecting() {
    while (true) {
        yield take(socketActions.openSocketConnection);
        yield race({
            task: call(initializeSocket),
            cancel: take(socketActions.closeSocketConnection),
        });
    }
}

export default function* socketSagas() {
    yield all([keepSocketReconnecting()]);
}