module.exports = {
    WELCOMING_MESSAGE: {
        type: 'SOCKET_CONNECTED',
        data: 'Connection to the socket established successfully'
    },
    ROUTE_TYPES: {
        FRONT: '/frontend',
    },
    AGENT_TYPES: {
        CLOUD_NETWORK: 'CLOUD_NETWORK',
        CLIENT: 'CLIENT',
        SERVER: 'SERVER',
        GREEN_ENERGY: 'GREEN_ENERGY',
        MONITORING: 'MONITORING',
        SCHEDULER: 'SCHEDULER'
    },
    JOB_STATUES: {
        CREATED: "CREATED",
        PROCESSED: "PROCESSED",
        IN_PROGRESS: "IN_PROGRESS",
        DELAYED: "DELAYED",
        FINISHED: "FINISHED",
        ON_BACK_UP: "ON_BACK_UP",
        ON_HOLD: "ON_HOLD",
        FAILED: "FAILED",
        REJECTED: "REJECTED"
    },
    EVENT_STATE: {
        ACTIVE: 'ACTIVE',
        INACTIVE: 'INACTIVE'
    },
    EVENT_TYPE: {
        POWER_SHORTAGE_EVENT: 'POWER_SHORTAGE_EVENT'
    }
}
