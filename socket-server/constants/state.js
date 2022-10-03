const { EVENT_STATE, EVENT_TYPE } = require("./constants")

module.exports = {
    INITIAL_NETWORK_AGENT_STATE: (data) => {
        return ({
            initialMaximumCapacity: data.maximumCapacity,
            currentMaximumCapacity: data.maximumCapacity,
            traffic: 0,
            numberOfExecutedJobs: 0,
            numberOfJobsOnHold: 0,
        })
    },
    INITIAL_POWER_SHORTAGE_STATE: {
        state: EVENT_STATE.ACTIVE,
        disabled: false,
        type: EVENT_TYPE.POWER_SHORTAGE_EVENT,
        occurenceTime: null,
        data: null
    }
}
