import {
    AgentEvent, AgentType,
    ClientAgent, CloudNetworkAgent, DEFAULT_NETWORK_AGENT_START_COMMONS, DEFAULT_POWER_SHORTAGE_EVENT, GreenEnergyAgent, DEFAULT_AGENT_START_COMMONS,
    JobStatus, MonitoringAgent, RegisterClientMessage,
    RegisterCloudNetworkMessage, RegisterGreenEnergyMessage,
    RegisterMonitoringMessage, RegisterServerMessage, ServerAgent, RegisterAgent
} from "@types";
export const registerNewAgent = (msg: RegisterAgent, type: AgentType) => {
    switch (type) {
        case AgentType.CLIENT:
            return registerClient(msg as RegisterClientMessage)
        case AgentType.CLOUD_NETWORK:
            return registerCloudNetwork(msg as RegisterCloudNetworkMessage)
        case AgentType.GREEN_ENERGY:
            return registerGreenEnergy(msg as RegisterGreenEnergyMessage)
        case AgentType.MONITORING:
            return registerMonitoring(msg as RegisterMonitoringMessage)
        case AgentType.SERVER:
            return registerServer(msg as RegisterServerMessage)
    }
}

const registerClient = (data: RegisterClientMessage): ClientAgent => {
    return {
        type: AgentType.CLIENT,
        jobStatusEnum: JobStatus.CREATED,
        events: [],
        ...DEFAULT_AGENT_START_COMMONS,
        ...data
    }
}

const registerCloudNetwork = (data: RegisterCloudNetworkMessage): CloudNetworkAgent => {
    return {
        type: AgentType.CLOUD_NETWORK,
        traffic: 0,
        totalNumberOfClients: 0,
        totalNumberOfExecutedJobs: 0,
        events: [],
        ...DEFAULT_AGENT_START_COMMONS,
        ...data
    }
}

const registerGreenEnergy = (data: RegisterGreenEnergyMessage): GreenEnergyAgent => {
    const events: AgentEvent[] = [DEFAULT_POWER_SHORTAGE_EVENT]

    return {
        type: AgentType.GREEN_ENERGY,
        events,
        ...DEFAULT_NETWORK_AGENT_START_COMMONS(data),
        ...DEFAULT_AGENT_START_COMMONS,
        ...data
    }
}

const registerServer = (data: RegisterServerMessage): ServerAgent => {
    const events: AgentEvent[] = [DEFAULT_POWER_SHORTAGE_EVENT]

    return {
        type: AgentType.SERVER,
        totalNumberOfClients: 0,
        backUpTraffic: 0,
        events,
        ...DEFAULT_NETWORK_AGENT_START_COMMONS(data),
        ...DEFAULT_AGENT_START_COMMONS,
        ...data
    }
}

const registerMonitoring = (data: RegisterMonitoringMessage): MonitoringAgent => {
    return {
        type: AgentType.MONITORING,
        events: [],
        ...DEFAULT_AGENT_START_COMMONS,
        ...data
    }
}