import { Agent } from "@types"

export const getAgentByName = (agents: Agent[], agentName?: string) => {
    return agents.find(agent => agent.name === agentName)
}
