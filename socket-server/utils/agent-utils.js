const { AGENT_TYPES, JOB_STATUES } = require("../constants/constants")
const { INITIAL_NETWORK_AGENT_STATE, INITIAL_POWER_SHORTAGE_STATE } = require("../constants/state")

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
    const events = [INITIAL_POWER_SHORTAGE_STATE]

    return {
        type: AGENT_TYPES.GREEN_ENERGY,
        events,
        isActive: false,
        ...INITIAL_NETWORK_AGENT_STATE(data),
        ...data
    }
}

const registerServer = (data) => {
    const events = [INITIAL_POWER_SHORTAGE_STATE]

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

const createCloudNetworkEdges = (agent) =>
    agent.serverAgents.map(serverAgent => createEdge(agent.name, serverAgent, true))

const createServerEdges = (agent) => {
    const uniEdge = createEdge(agent.name, agent.cloudNetworkAgent, false)
    const cloudNetworkEdge = createEdge(agent.name, agent.cloudNetworkAgent, true)
    const edges = agent.greenEnergyAgents.map(greenAgent => createEdge(agent.name, greenAgent, true))

    edges.push(uniEdge)
    edges.push(cloudNetworkEdge)

    return edges
}

const createGreenEnergyEdges = (agent) => {
    const uniEdgeMonitoring = createEdge(agent.name, agent.monitoringAgent, false)
    const uniEdgeServer = createEdge(agent.name, agent.serverAgent, false)
    const directedEdgeMonitoring = createEdge(agent.name, agent.monitoringAgent, true)
    const directedEdgeServer = createEdge(agent.name, agent.serverAgent, true)

    return [uniEdgeMonitoring, uniEdgeServer, directedEdgeMonitoring, directedEdgeServer]
}

const createMonitoringEdges = (agent) =>
    [createEdge(agent.name, agent.greenEnergyAgent, true)]


const createEdge = (source, target, isDirected) => {
    const id = isDirected ?
        [source, target].join('-') :
        [source, target, 'BI'].join('-')
    const type = isDirected ? 'directed' : 'unidirected'
    return ({ data: { id, source, target, type }, state: 'inactive' })
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
        }
    },
    createAgentConnections: function (agent) {
        switch (agent.type) {
            case AGENT_TYPES.CLOUD_NETWORK: return createCloudNetworkEdges(agent)
            case AGENT_TYPES.SERVER: return createServerEdges(agent)
            case AGENT_TYPES.GREEN_ENERGY: return createGreenEnergyEdges(agent)
            case AGENT_TYPES.MONITORING: return createMonitoringEdges(agent)
            default: return []
        }
    }
}
