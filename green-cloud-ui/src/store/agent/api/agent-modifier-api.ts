import { Agent, AgentType, CloudNetworkAgent, CommonNetworkAgentInterface, ServerAgent } from "@types";

export const changeAgentTraffic = (agent: Agent, powerInUse: number) => {
    if (agent.type === AgentType.CLOUD_NETWORK) {
        const cna = agent as CloudNetworkAgent
        cna.isActive = powerInUse > 0
        cna.traffic = calculateAgentTraffic(cna.maximumCapacity, powerInUse)
    } else {
        const networkAgent = agent as CommonNetworkAgentInterface
        networkAgent.traffic = calculateAgentTraffic(networkAgent.currentMaximumCapacity, powerInUse)
    }
}

export const changeAgentBackUpTraffic = (agent: ServerAgent, backUpPower: number) => {
    agent.backUpTraffic = calculateAgentTraffic(agent.currentMaximumCapacity, backUpPower)
}

export const changeAgentActivness = (agent: CommonNetworkAgentInterface, isActive: boolean) => {
    agent.isActive = isActive
}

export const changeAgentJobsOnHold = (agent: CommonNetworkAgentInterface, jobsOnHold: number) => {
    agent.numberOfJobsOnHold = jobsOnHold
}

export const changeAgentMaximumCapacity = (agent: CommonNetworkAgentInterface, power: number, maxCapacity: number) => {
    agent.currentMaximumCapacity = maxCapacity
    agent.traffic = calculateAgentTraffic(agent.currentMaximumCapacity, power)
}

const calculateAgentTraffic = (maxCapacity: number, powerInUse: number) =>
    maxCapacity === 0 ? 0 : powerInUse / maxCapacity * 100
