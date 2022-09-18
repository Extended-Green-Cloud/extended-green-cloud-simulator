import { Agent, AgentType, CloudNetworkAgent, CloudNetworkTraffic, GraphEdge, AgentNode, GreenEnergyAgent, GreenEnergyState, MonitoringAgent, ServerAgent, ServerState } from "@types"

export const createEdgesForAgent = (agent: Agent): GraphEdge[] => {
    switch (agent.type) {
        case AgentType.CLOUD_NETWORK: return createCloudNetworkEdges(agent as CloudNetworkAgent)
        case AgentType.SERVER: return createServerEdges(agent as ServerAgent)
        case AgentType.GREEN_ENERGY: return createGreenEnergyEdges(agent as GreenEnergyAgent)
        case AgentType.MONITORING: return createMonitoringEdges(agent as MonitoringAgent)
        default: return []
    }
}

export const createNodeForAgent = (agent: Agent): AgentNode => {
    const node = { id: agent.name, label: agent.name, type: agent.type }
    switch (agent.type) {
        case AgentType.CLOUD_NETWORK:
            return { traffic: getCloudNetworkState(agent as CloudNetworkAgent), ...node }
        case AgentType.SERVER:
            return { state: getServerState(agent as ServerAgent), ...node }
        case AgentType.GREEN_ENERGY:
            return { state: getGreenEnergyState(agent as GreenEnergyAgent), ...node }
        case AgentType.MONITORING: return node
        default: return node
    }
}

const getCloudNetworkState = (cloudNetwork: CloudNetworkAgent): CloudNetworkTraffic => {
    if (cloudNetwork.traffic > 85)
        return CloudNetworkTraffic.HIGH
    if (cloudNetwork.traffic > 50)
        return CloudNetworkTraffic.MEDIUM

    return cloudNetwork.traffic > 0 ?
        CloudNetworkTraffic.LOW :
        CloudNetworkTraffic.INACTIVE
}


const getServerState = (server: ServerAgent): ServerState => {
    if (server.numberOfJobsOnHold > 0)
        return ServerState.ON_HOLD
    if (server.backUpTraffic > 0)
        return ServerState.BACK_UP

    return server.isActive ?
        ServerState.ACTIVE :
        ServerState.INACTIVE
}

const getGreenEnergyState = (greenEnergy: GreenEnergyAgent): GreenEnergyState => {
    if (greenEnergy.numberOfJobsOnHold > 0)
        return GreenEnergyState.ON_HOLD

    return greenEnergy.isActive ?
        GreenEnergyState.ACTIVE :
        GreenEnergyState.INACTIVE
}


const createCloudNetworkEdges = (agent: CloudNetworkAgent): GraphEdge[] =>
    agent.serverAgents.map(serverAgent => createEdge(agent, agent.name, serverAgent, true))

const createServerEdges = (agent: ServerAgent): GraphEdge[] => {
    const uniEdge = createEdge(agent, agent.name, agent.cloudNetworkAgent, false)
    const cloudNetworkEdge = createEdge(agent, agent.name, agent.cloudNetworkAgent, true)
    const edges = agent.greenEnergyAgents.map(greenAgent => createEdge(agent, agent.name, greenAgent, true))

    edges.push(uniEdge)
    edges.push(cloudNetworkEdge)

    return edges
}

const createGreenEnergyEdges = (agent: GreenEnergyAgent): GraphEdge[] => {
    const uniEdgeMonitoring = createEdge(agent, agent.name, agent.monitoringAgent, false)
    const uniEdgeServer = createEdge(agent, agent.name, agent.serverAgent, false)
    const directedEdgeMonitoring = createEdge(agent, agent.name, agent.monitoringAgent, true)
    const directedEdgeServer = createEdge(agent, agent.name, agent.serverAgent, true)

    return [uniEdgeMonitoring, uniEdgeServer, directedEdgeMonitoring, directedEdgeServer]
}

const createMonitoringEdges = (agent: MonitoringAgent): GraphEdge[] =>
    [createEdge(agent, agent.name, agent.greenEnergyAgent, true)]


const createEdge = (agent: Agent, source: string, target: string, isDirected: boolean): GraphEdge => {
    const id = isDirected ? [source, target].join('-') : [source, target, 'BI'].join('-')
    const prevEdge = agent.edges?.find(edge => edge.data.id === id)

    if(!prevEdge) {
        const type = isDirected ? 'directed' : 'unidirected'
        const state = 'inactive'
        return({ data: { id: isDirected ? id : [id, 'BI'].join('-'), source, target, type, state }, state })
    } else {
        const {state, ...prevState} = prevEdge.data
        return ({ data: { state: prevEdge.state, ...prevState }, state: prevEdge.state })
    }
}