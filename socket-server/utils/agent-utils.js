const { AGENT_TYPES, JOB_STATUES } = require("../constants/constants")
const { INITIAL_NETWORK_AGENT_STATE, INITIAL_POWER_SHORTAGE_STATE } = require("../constants/state")

const registerScheduler = (data) => {
    return {
        type: AGENT_TYPES.SCHEDULER,
        scheduledJobs: [],
        events: [],
        isActive: true,
        ...data
    }
}

const registerClient = (data) => {
    return {
        type: AGENT_TYPES.CLIENT,
        jobStatusEnum: JOB_STATUES.CREATED,
        events: [],
        isActive: false,
        ...data
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
        ...data
    }
}

const registerGreenEnergy = (data) => {
    const events = [structuredClone(INITIAL_POWER_SHORTAGE_STATE)]

    return {
        type: AGENT_TYPES.GREEN_ENERGY,
        events,
        isActive: false,
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
        ...INITIAL_NETWORK_AGENT_STATE(data),
        ...data
    }
}

const registerMonitoring = (data) => {
    return {
        type: AGENT_TYPES.MONITORING,
        events: [],
        isActive: false,
        ...data
    }
}

const createServerEdges = (agent) => {
    const cloudNetworkEdge = createEdge(agent.name, agent.cloudNetworkAgent)
    
    return [cloudNetworkEdge]
}

const createGreenEnergyEdges = (agent) => {
    const edgeMonitoring = createEdge(agent.name, agent.monitoringAgent)
    const edgeServer = createEdge(agent.name, agent.serverAgent)

    return [edgeMonitoring, edgeServer]
}

const createEdge = (source, target) => {
    const id = [source, target, 'BI'].join('-')
    return ({ data: { id, source, target }, state: 'inactive' })
}

module.exports = {
    getAgentByName: function (agents, agentName) {
        return agents.find(agent => agent.name === agentName)
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
    createAgentConnections: function (agent) {
        switch (agent.type) {
            case AGENT_TYPES.SERVER: return createServerEdges(agent)
            case AGENT_TYPES.GREEN_ENERGY: return createGreenEnergyEdges(agent)
            default: return []
        }
    }
}
