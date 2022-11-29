const { AGENT_TYPES, JOB_STATUES } = require("../constants/constants")
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

        state.agents.connections
            .forEach(connection => {
                if (connection.data.source === agent.name || connection.data.target === agent.name) {
                    const secondAgent = connection.data.source === agent.name ?
                        getAgentByName(state.agents.agents, connection.data.target) :
                        getAgentByName(state.agents.agents, connection.data.source)
                    connection.state = agent.isActive && secondAgent.isActive ? 'active' : 'inactive'
                }
            })
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

const handleSetSuccessRatio = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const successRatio = msg.data

    if (agent) {
        if (agent?.type === AGENT_TYPES.SERVER || 
            agent?.type === AGENT_TYPES.GREEN_ENERGY ||
            agent?.type === AGENT_TYPES.CLOUD_NETWORK) {
            agent.successRatio = successRatio
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
    const jobStatus = msg.data.status
    const splitJobId = msg.data.splitJobId

    if (agent) {
        if(jobStatus === JOB_STATUES.FAILED) {
            agent.status = jobStatus
            if (agent.isSplit) {
                agent.splitJobs.forEach(job => job.status = jobStatus)
            }
            return
        }
        
        if (splitJobId) {
            const splitJob = agent.splitJobs.find(job => job.splitJobId === splitJobId)
            if (splitJob) {
                splitJob.status = jobStatus
            }
        } else {
            agent.status = jobStatus
        }
    }
}

const handleUpdateJobQueue = (state, msg) => {
    const scheduler = state.network.scheduler
    scheduler.scheduledJobs = msg.data.map(job => job.jobId)
}

const handleRegisterAgent = (state, msg) => {
    const agentType = msg.agentType
    const registerData = msg.data

    if (agentType === AGENT_TYPES.SCHEDULER) {
        const newAgent = registerAgent(registerData, agentType)
        state.network.scheduler = newAgent
    }
    else if (!getAgentByName(state.agents.agents, registerData.name)) {
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

const handleRegisterManaging = (state, msg) => {
    const adaptationGoals = msg.data
    state.managingSystem.adaptationGoals = adaptationGoals
}

const handleUpdateIndicators = (state, msg) => {
    state.managingSystem.systemIndicator = msg.systemIndicator
    const goalQualities = Object.entries(msg.data).map(([key, value]) => ({ id: key, quality: value }))
    state.managingSystem.goalQualityIndicators = goalQualities
}

const handleAddAdaptationLog = (state, msg) => {
    state.managingSystem.adaptationLogs.push(msg.data)
}

const handleIncrementWeakAdaptations = (state, msg) => {
    state.managingSystem.weakAdaptations += 1
    state.managingSystem.performedAdaptations += 1
}

const handleIncrementStrongAdaptations = (state, msg) => {
    state.managingSystem.strongAdaptations += 1
    state.managingSystem.performedAdaptations += 1
}

const handleJobSplit = (state, msg) => {
    const clients = state.agents.clients
    const clientForSplit = clients.find(client => client.job.jobId === msg.jobId)

    if (clientForSplit) {
        const splitData = msg.data.map(splitJob => ({ status: JOB_STATUES.CREATED, ...splitJob }))
        clientForSplit.isSplit = true
        clientForSplit.splitJobs = splitData
    }
}

module.exports = {
    MESSAGE_HANDLERS: {
        INCREMENT_FINISHED_JOBS: handleIncrementFinishJobs,
        INCREMENT_FAILED_JOBS: handleIncrementFailedJobs,
        UPDATE_CURRENT_CLIENTS: handleUpdateCurrentClients,
        UPDATE_CURRENT_PLANNED_JOBS: handleUpdateCurrentPlannedJobs,
        UPDATE_CURRENT_ACTIVE_JOBS: handleUpdateCurrentActiveJobs,
        UPDATE_JOB_QUEUE: handleUpdateJobQueue,
        SET_MAXIMUM_CAPACITY: handleSetMaximumCapacity,
        SET_TRAFFIC: handleSetTraffic,
        SET_IS_ACTIVE: handleSetActive,
        SET_JOBS_COUNT: handleSetJobsCount,
        SET_ON_HOLD_JOBS_COUNT: handleSetJobsOnHold,
        SET_CLIENT_NUMBER: handleSetClientNumber,
        SET_CLIENT_JOB_STATUS: handleSetClientJobStatus,
        SET_SERVER_BACK_UP_TRAFFIC: handleSetBackUpTraffic,
        SET_JOB_SUCCESS_RATIO: handleSetSuccessRatio,
        SPLIT_JOB: handleJobSplit,
        REGISTER_AGENT: handleRegisterAgent,
        REGISTER_MANAGING: handleRegisterManaging,
        UPDATE_INDICATORS: handleUpdateIndicators,
        ADD_ADAPTATION_LOG: handleAddAdaptationLog,
        INCREMENT_WEAK_ADAPTATIONS: handleIncrementWeakAdaptations,
        INCREMENT_STRONG_ADAPTATIONS: handleIncrementStrongAdaptations
    }
}
