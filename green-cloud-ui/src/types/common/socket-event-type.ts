import { NotUndefined } from "@redux-saga/types"
import { END } from "redux-saga"

export type SocketEvent = {
    event: any,
    emitter: ((input: NotUndefined | END) => void)
}