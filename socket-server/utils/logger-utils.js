module.exports = {
    logNewMessage: function (state, msg) {
        if (msg.type === 'UPDATE_JOB_QUEUE') {
            console.log(`[${(new Date()).toLocaleTimeString()}] Retrieved message: UPDATE_JOB_QUEUE`)
        } else {
            console.log(`[${(new Date()).toLocaleTimeString()}] Retrieved message: ${JSON.stringify(msg)}`)
        }
    },
    logUserConnected: function () {
        console.log(`[${(new Date()).toLocaleTimeString()}] User connected to WebSocket`)
    },
    logStateReset: function () {
        console.log(`[${(new Date()).toLocaleTimeString()}] State has been reset`)
    },
    logState: function (state) {
        console.log(state)
    },
    logPowerShortageEvent: function (agentName, actionState) {
        console.log(`[${(new Date()).toLocaleTimeString()}] Power shortage ${actionState} for ${agentName}`)
    },
}