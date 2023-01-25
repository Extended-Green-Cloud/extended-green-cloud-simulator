const { AGENT_TYPES, JOB_STATUES } = require("../constants/constants")
const { getAgentByName, getAgentNodeById, getNewTraffic, createAgentConnections, registerAgent, createEdge, createNodeForAgent, getNodeState, getNewCloudNetworkTraffic } = require("./agent-utils")
const sleep = require('util').promisify(setTimeout)

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
    const node = getAgentNodeById(state.graph.nodes, msg.agentName)
    const { maximumCapacity, powerInUse } = msg.data

    if (agent) {    
        if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
            agent.maximumCapacity = maximumCapacity
            getNewCloudNetworkTraffic(agent, powerInUse, state)
        } else {
            agent.currentMaximumCapacity = maximumCapacity
            agent.traffic = getNewTraffic(maximumCapacity, powerInUse)
        }

        if (node) {
            node.state = getNodeState(agent)
        }
    }
}

const handleSetTraffic = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const node = getAgentNodeById(state.graph.nodes, msg.agentName)
    const powerInUse = msg.data

    if (agent) {
        if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
            getNewCloudNetworkTraffic(agent, powerInUse, state)
        } else {
            agent.traffic = getNewTraffic(agent.currentMaximumCapacity, powerInUse)
        }

        if (node) {
            node.state = getNodeState(agent)
        }
    }
}

const handleSetBackUpTraffic = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const node = getAgentNodeById(state.graph.nodes, msg.agentName)
    const backUpPower = msg.data

    if (agent) {
        agent.backUpTraffic = getNewTraffic(agent.currentMaximumCapacity, backUpPower)

        if (node) {
            node.state = getNodeState(agent)
        }
    }
}

const handleSetActive = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const node = getAgentNodeById(state.graph.nodes, msg.agentName)

    if (agent) {
        agent.isActive = msg.data

        if (node) {
            node.state = getNodeState(agent)
        }

        state.graph.connections
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
    const node = getAgentNodeById(state.graph.nodes, msg.agentName)

    if (agent) {
        agent.numberOfJobsOnHold = msg.data

        if (node) {
            node.state = getNodeState(agent)
        }
    }
}

const handleSetJobsCount = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const node = getAgentNodeById(state.graph.nodes, msg.agentName)
    const jobsCount = msg.data

    if (agent) {
        if (agent.type === AGENT_TYPES.SERVER || agent?.type === AGENT_TYPES.GREEN_ENERGY) {
            agent.numberOfExecutedJobs = jobsCount
        } else if (agent.type === AGENT_TYPES.CLOUD_NETWORK) {
            agent.totalNumberOfExecutedJobs = jobsCount
        }

        if (node) {
            node.state = getNodeState(agent)
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

const handleWeatherPredictionError = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)
    const error = msg.data

    if (agent && agent.type === AGENT_TYPES.GREEN_ENERGY) {
        agent.weatherPredictionError = error
    }
}

const handleSetClientJobStatus = (state, msg) => {
    const agent = getAgentByName(state.agents.clients, msg.agentName)
    const jobStatus = msg.data.status
    const splitJobId = msg.data.splitJobId

    if (agent) {
        if (jobStatus === JOB_STATUES.FAILED) {
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

const handleSetClientJobTimeFrame = (state, msg) => {
    const agent = getAgentByName(state.agents.clients, msg.agentName)
    const { start, end } = msg.data
    const splitJobId = msg.data.splitJobId

    if (agent) {
        if (splitJobId) {
            const splitJob = agent.splitJobs.find(job => job.splitJobId === splitJobId)
            if (splitJob) {
                splitJob.start = start
                splitJob.end = end
            }
        } else {
            agent.job.start = start
            agent.job.end = end
        }
    }
}

const handleSetClientJobDurationMap = (state, msg) => {
    const agent = getAgentByName(state.agents.clients, msg.agentName)

    if (agent) {
        agent.durationMap = msg.data
    }
}

const handleUpdateJobQueue = (state, msg) => {
    if (state.agents.scheduler !== null) {
        const scheduler = state.agents.scheduler
        scheduler.scheduledJobs = msg.data.map(job => job.jobId)
    }
}

const handleRegisterAgent = (state, msg) => {
    const agentType = msg.agentType
    const registerData = msg.data

    if (agentType === AGENT_TYPES.SCHEDULER) {
        const newAgent = registerAgent(registerData, agentType)

        state.agents.scheduler = newAgent
        state.graph.nodes.push(createNodeForAgent(newAgent))
    }
    else if (!getAgentByName(state.agents.agents, registerData.name)) {
        const newAgent = registerAgent(registerData, agentType)

        if (newAgent) {
            if (agentType === AGENT_TYPES.CLIENT)
                state.agents.clients.push(newAgent)
            else {
                state.agents.agents.push(newAgent)
                state.graph.nodes.push(createNodeForAgent(newAgent))
            }

            Object.assign(state.graph.connections,
                state.graph.connections.concat((createAgentConnections(newAgent, state))))
        }
    }
}

const handleUpdateServerConnection = (state, msg) => {
    const agent = getAgentByName(state.agents.agents, msg.agentName)

    if (agent) {
        const { isConnected, serverName } = msg.data

        if (isConnected) {
            agent.connectedServers.push(serverName)
            Object.assign(state.graph.connections, state.graph.connections.concat(createEdge(agent.name, serverName)))
        } else {
            agent.connectedServers = agent.connectedServers.filter(server => server !== serverName)
            state.graph.connections = state.graph.connections.filter(edge => edge.data.id !== [agent.name, serverName, 'BI'].join('-'))
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

    if (msg.data.agentName) {
        const { agentName } = msg.data
        const node = getAgentNodeById(state.graph.nodes, agentName)

        if (node) {
            node.adaptation = 'active'
            finishAdaptation(node)
        }
    }
}

async function finishAdaptation(node) {
    await sleep(1500)
    node.adaptation = 'inactive'
}

const handleIncrementWeakAdaptations = (state, _msg) => {
    state.managingSystem.weakAdaptations += 1
    state.managingSystem.performedAdaptations += 1
}

const handleIncrementStrongAdaptations = (state, _msg) => {
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

const handleUpdatePowerPriority = (state, msg) => state.agents.scheduler.powerPriority = msg.data
const handleUpdateDeadlinePriority = (state, msg) => state.agents.scheduler.deadlinePriority = msg.data

const handleRemoveAgent = (state, msg) => {
    const agentName = msg.agentName

    state.agents.agents = state.agents.agents.filter(agent => agent.name !== agentName)
    state.graph.nodes = state.graph.nodes.filter(node => node.id !== agentName)
    state.graph.connections =
        state.graph.connections.filter(edge => edge.data.target !== agentName && edge.data.source !== agentName)
}

module.exports = {
    MESSAGE_HANDLERS: {
        INCREMENT_FINISHED_JOBS: handleIncrementFinishJobs,
        INCREMENT_FAILED_JOBS: handleIncrementFailedJobs,
        INCREMENT_WEAK_ADAPTATIONS: handleIncrementWeakAdaptations,
        INCREMENT_STRONG_ADAPTATIONS: handleIncrementStrongAdaptations,
        UPDATE_CURRENT_CLIENTS: handleUpdateCurrentClients,
        UPDATE_CURRENT_PLANNED_JOBS: handleUpdateCurrentPlannedJobs,
        UPDATE_CURRENT_ACTIVE_JOBS: handleUpdateCurrentActiveJobs,
        UPDATE_JOB_QUEUE: handleUpdateJobQueue,
        UPDATE_SERVER_CONNECTION: handleUpdateServerConnection,
        UPDATE_INDICATORS: handleUpdateIndicators,
        UPDATE_SCHEDULER_POWER_PRIORITY: handleUpdatePowerPriority,
        UPDATE_SCHEDULER_DEADLINE_PRIORITY: handleUpdateDeadlinePriority,
        SET_MAXIMUM_CAPACITY: handleSetMaximumCapacity,
        SET_TRAFFIC: handleSetTraffic,
        SET_IS_ACTIVE: handleSetActive,
        SET_JOBS_COUNT: handleSetJobsCount,
        SET_ON_HOLD_JOBS_COUNT: handleSetJobsOnHold,
        SET_CLIENT_NUMBER: handleSetClientNumber,
        SET_CLIENT_JOB_STATUS: handleSetClientJobStatus,
        SET_CLIENT_JOB_TIME_FRAME: handleSetClientJobTimeFrame,
        SET_CLIENT_JOB_DURATION_MAP: handleSetClientJobDurationMap,
        SET_SERVER_BACK_UP_TRAFFIC: handleSetBackUpTraffic,
        SET_JOB_SUCCESS_RATIO: handleSetSuccessRatio,
        SET_WEATHER_PREDICTION_ERROR: handleWeatherPredictionError,
        SPLIT_JOB: handleJobSplit,
        REGISTER_AGENT: handleRegisterAgent,
        REMOVE_AGENT: handleRemoveAgent,
        REGISTER_MANAGING: handleRegisterManaging,
        ADD_ADAPTATION_LOG: handleAddAdaptationLog,
    }
}
