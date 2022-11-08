const { AGENT_TYPES } = require("../constants/constants")
const { getAgentByName, getNewTraffic, createAgentConnections, registerAgent } = require("./agent-utils")

const handleIncrementFinishJobs = (state, msg) => {
    state.network.finishedJobsNo += msg.data
}

const handleIncrementFailedJobs = (state, msg) => {
    state.network.failedJobsNo += msg.data
}

const handleUpdateCurrentClients = (state, msg) => {
    state.network.currClientsNo += msg.data
}

const handleUpdateCurrentPlannedJobs = (state, msg) => {
    state.network.currPlannedJobsNo += msg.data
}

const handleUpdateCurrentActiveJobs = (state, msg) => {
    state.network.currActiveJobsNo += msg.data
}

const handleUpdateTotalPrice = (state, msg) => {
    state.network.totalPrice = msg.data
}

const handleSetMaximumCapacity = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const { maximumCapacity, powerInUse } = msg.data

    if (agent) {
        agent.currentMaximumCapacity = maximumCapacity
        agent.traffic = getNewTraffic(maximumCapacity, powerInUse)
    }
}

const handleSetTraffic = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const powerInUse = msg.data

    if (agent) {
        if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
            agent.isActive = powerInUse > 0
            agent.traffic = getNewTraffic(agent.maximumCapacity, powerInUse)
        } else {
            agent.traffic = getNewTraffic(agent.currentMaximumCapacity, powerInUse)
        }
    }
}

const handleSetBackUpTraffic = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const backUpPower = msg.data

    if (agent) {
        agent.backUpTraffic = getNewTraffic(agent.currentMaximumCapacity, backUpPower)
    }
}

const handleSetActive = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)

    if (agent) {
        agent.isActive = msg.data

        if (agent.type === AGENT_TYPES.SERVER) {
            state.agents.connections
                .filter(connection => connection.data.type === 'unidirected')
                .forEach(connection => {
                    if (connection.data.source === agent.name || connection.data.target === agent.name) {
                        connection.state = agent.isActive ? 'active' : 'inactive'
                    }
                })
        }
    }
}

const handleSetJobsOnHold = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)

    if (agent) {
        agent.numberOfJobsOnHold = msg.data
    }
}

const handleSetJobsCount = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const jobsCount = msg.data

    if (agent) {
        if (agent.type === AGENT_TYPES.SERVER || agent?.type === AGENT_TYPES.GREEN_ENERGY) {
            agent.numberOfExecutedJobs = jobsCount
        } else if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
            agent.totalNumberOfExecutedJobs = jobsCount
        }
    }
}

const handleSetClientNumber = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const clientNumber = msg.data

    if (agent) {
        agent.totalNumberOfClients = clientNumber
    }
}

const handleSetClientJobStatus = (state, msg) => {
    const agent = getAgentByName(state.agents.clients, msg.agentName)
    const jobStatus = msg.data

    if (agent) {
        agent.jobStatusEnum = jobStatus
    }
}

const handleRegisterAgent = (state, msg) => {
    const agentType = msg.agentType
    const registerData = msg.data

    if (!getAgentByName(state.agents.agents, registerData.name)) {
        const newAgent = registerAgent(registerData, agentType)

        if (newAgent) {
            if (agentType === AGENT_TYPES.CLIENT)
                state.agents.clients.push(newAgent)
            else
                state.agents.agents.push(newAgent)

            Object.assign(state.agents.connections,
                state.agents.connections.concat((createAgentConnections(newAgent))))
        }
    }
}

module.exports = {
    MESSAGE_HANDLERS: {
        INCREMENT_FINISHED_JOBS: handleIncrementFinishJobs,
        INCREMENT_FAILED_JOBS: handleIncrementFailedJobs,
        UPDATE_CURRENT_CLIENTS: handleUpdateCurrentClients,
        UPDATE_CURRENT_PLANNED_JOBS: handleUpdateCurrentPlannedJobs,
        UPDATE_CURRENT_ACTIVE_JOBS: handleUpdateCurrentActiveJobs,
        UPDATE_TOTAL_PRICE: handleUpdateTotalPrice,
        SET_MAXIMUM_CAPACITY: handleSetMaximumCapacity,
        SET_TRAFFIC: handleSetTraffic,
        SET_IS_ACTIVE: handleSetActive,
        SET_JOBS_COUNT: handleSetJobsCount,
        SET_ON_HOLD_JOBS_COUNT: handleSetJobsOnHold,
        SET_CLIENT_NUMBER: handleSetClientNumber,
        SET_CLIENT_JOB_STATUS: handleSetClientJobStatus,
        SET_SERVER_BACK_UP_TRAFFIC: handleSetBackUpTraffic,
        REGISTER_AGENT: handleRegisterAgent
    }
}
