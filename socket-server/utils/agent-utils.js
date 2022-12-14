const { AGENT_TYPES, JOB_STATUES } = require("../constants/constants")
const { INITIAL_NETWORK_AGENT_STATE, INITIAL_POWER_SHORTAGE_STATE } = require("../constants/state")

const registerScheduler = (data) => {
    return {
        type: AGENT_TYPES.SCHEDULER,
        scheduledJobs: [],
        events: [],
        isActive: true,
        adaptation: 'inactive',
        ...data
    }
}

const registerClient = (data) => {
    const { name, ...jobData } = data
    return {
        type: AGENT_TYPES.CLIENT,
        status: JOB_STATUES.CREATED,
        events: [],
        name,
        isActive: false,
        adaptation: 'inactive',
        isSplit: false,
        splitJobs: [],
        durationMap: null,
        job: jobData
    }
}

const registerCloudNetwork = (data) => {
    return {
        type: AGENT_TYPES.CLOUD_NETWORK,
        traffic: 0,
        totalNumberOfClients: 0,
        totalNumberOfExecutedJobs: 0,
        events: [],
        isActive: false,
        adaptation: 'inactive',
        ...data
    }
}

const registerGreenEnergy = (data) => {
    const events = [structuredClone(INITIAL_POWER_SHORTAGE_STATE)]

    return {
        type: AGENT_TYPES.GREEN_ENERGY,
        events,
        isActive: false,
        adaptation: 'inactive',
        connectedServers: [data.serverAgent],
        ...INITIAL_NETWORK_AGENT_STATE(data),
        ...data
    }
}

const registerServer = (data) => {
    const events = [structuredClone(INITIAL_POWER_SHORTAGE_STATE)]

    return {
        type: AGENT_TYPES.SERVER,
        totalNumberOfClients: 0,
        backUpTraffic: 0,
        events,
        isActive: false,
        adaptation: 'inactive',
        ...INITIAL_NETWORK_AGENT_STATE(data),
        ...data
    }
}

const registerMonitoring = (data) => {
    return {
        type: AGENT_TYPES.MONITORING,
        events: [],
        isActive: false,
        adaptation: 'inactive',
        ...data
    }
}

const createNodeForAgent = (agent) => {
    const node = {
        id: agent.name,
        label: agent.name,
        type: agent.type,
        adaptation: agent.adaptation,
    }
    switch (agent.type) {
        case AGENT_TYPES.CLOUD_NETWORK:
        case AGENT_TYPES.GREEN_ENERGY:
        case AGENT_TYPES.SERVER:
            return { state: 'inactive', ...node }
        default:
            return node
    }
}

const getNodeState = (agent) => {
    switch (agent.type) {
        case AGENT_TYPES.CLOUD_NETWORK:
            return getCloudNetworkState(agent)
        case AGENT_TYPES.GREEN_ENERGY:
            return getGreenEnergyState(agent)
        case AGENT_TYPES.SERVER:
            return getServerState(agent)
        default:
            return null
    }
}

const getCloudNetworkState = (cloudNetwork) => {
    if (cloudNetwork.traffic > 85) return 'high'
    if (cloudNetwork.traffic > 50) return 'medium'

    return cloudNetwork.traffic > 0 ? 'low' : 'inactive'
}

const getServerState = (server) => {
    if (server.numberOfJobsOnHold > 0) return 'on_hold'
    if (server.backUpTraffic > 0) return 'back_up'

    return server.isActive ? 'active' : 'inactive'
}

const getGreenEnergyState = (greenEnergy) => {
    if (greenEnergy.numberOfJobsOnHold > 0 && greenEnergy.numberOfExecutedJobs > 0)
        return 'on_hold'

    return greenEnergy.isActive ? 'active' : 'inactive'
}


const createCloudNetworkEdges = (agent, state) => {
    const schedulerEdge = createEdge(agent.name, state.agents.scheduler.name)

    return [schedulerEdge]
}

const createServerEdges = (agent) => {
    const cloudNetworkEdge = createEdge(agent.name, agent.cloudNetworkAgent)

    return [cloudNetworkEdge]
}

const createGreenEnergyEdges = (agent) => {
    const edgeMonitoring = createEdge(agent.name, agent.monitoringAgent)
    const edgesServers = agent.connectedServers.map(server => createEdge(agent.name, server))

    return edgesServers.concat(edgeMonitoring)
}

const createEdge = (source, target) => {
    const id = [source, target, 'BI'].join('-')
    return ({ data: { id, source, target }, state: 'inactive' })
}

module.exports = {
    getAgentByName: function (agents, agentName) {
        return agents.find(agent => agent.name === agentName)
    },
    getAgentNodeById: function (nodes, id) {
        return nodes.find(node => node.id === id)
    },
    getNewTraffic: function (maximumCapacity, powerInUse) {
        return maximumCapacity === 0 ? 0 : powerInUse / maximumCapacity * 100
    },
    registerAgent: function (data, type) {
        switch (type) {
            case AGENT_TYPES.CLIENT:
                return registerClient(data)
            case AGENT_TYPES.CLOUD_NETWORK:
                return registerCloudNetwork(data)
            case AGENT_TYPES.GREEN_ENERGY:
                return registerGreenEnergy(data)
            case AGENT_TYPES.MONITORING:
                return registerMonitoring(data)
            case AGENT_TYPES.SERVER:
                return registerServer(data)
            case AGENT_TYPES.SCHEDULER:
                return registerScheduler(data)
        }
    },
    createAgentConnections: function (agent, state) {
        switch (agent.type) {
            case AGENT_TYPES.SERVER: return createServerEdges(agent)
            case AGENT_TYPES.GREEN_ENERGY: return createGreenEnergyEdges(agent)
            case AGENT_TYPES.CLOUD_NETWORK: return createCloudNetworkEdges(agent, state)
            default: return []
        }
    },
    createEdge,
    createNodeForAgent,
    getNodeState
}
