import { Agent, AgentType, ClientAgent, CloudNetworkAgent, GraphEdge, GreenEnergyAgent, JobStatus, MonitoringAgent, RegisterClientMessage, RegisterCloudNetworkMessage, RegisterGreenEnergyMessage, RegisterMonitoringMessage, RegisterServerMessage, ServerAgent } from "@types";
import { createEdgesForAgent } from "@utils";

export const getAgentByName = (agents: Agent[], agentName?: string) => {
    console.log('lol')
    return agents.find(agent => agent.name === agentName)
}

export const calculateAgentTraffic = (maxCapacity: number, powerInUse: number) =>
    maxCapacity === 0 ? 0 : powerInUse / maxCapacity * 100

export const registerClient = (data: RegisterClientMessage): ClientAgent => {
    const agent = { type: AgentType.CLIENT, jobStatusEnum: JobStatus.CREATED, ...data }
    return ({ edges: createEdgesForAgent(agent), ...agent })
}

export const registerCloudNetwork = (data: RegisterCloudNetworkMessage): CloudNetworkAgent => {
    const agent = { type: AgentType.CLOUD_NETWORK, traffic: 0, totalNumberOfClients: 0, totalNumberOfExecutedJobs: 0, ...data }
    return ({ edges: createEdgesForAgent(agent), ...agent })
}

export const registerGreenEnergy = (data: RegisterGreenEnergyMessage): GreenEnergyAgent => {
    const agent = { type: AgentType.GREEN_ENERGY, initialMaximumCapacity: data.maximumCapacity, currentMaximumCapacity: data.maximumCapacity, isActive: false, traffic: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, ...data }
    return ({ edges: createEdgesForAgent(agent), ...agent })
}

export const registerServer = (data: RegisterServerMessage): ServerAgent => {
    const agent = { type: AgentType.SERVER, initialMaximumCapacity: data.maximumCapacity, currentMaximumCapacity: data.maximumCapacity, isActive: false, traffic: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, totalNumberOfClients: 0, backUpTraffic: 0, ...data }
    return ({ edges: createEdgesForAgent(agent), ...agent })
}

export const registerMonitoring = (data: RegisterMonitoringMessage): MonitoringAgent => {
    const agent = { type: AgentType.MONITORING, ...data }
    return ({ edges: createEdgesForAgent(agent), ...agent })
}

export const getEdges = (agent: Agent, targetAgents: string[]) =>
    targetAgents
        .map(target => [agent.name, target].join('-'))
        .map(id => agent.edges?.find(edge => edge.data.id === id))


export const changeEdgeState = (edges: (GraphEdge | undefined)[], state: string) => {
    edges.forEach(edge => {
        if (edge) {
            edge.state = state
        }
    })
}