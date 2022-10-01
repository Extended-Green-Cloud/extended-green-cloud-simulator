import { Agent } from "@types"

/**
 * Method retrieves an agent by its name
 * 
 * @param {Agent[]}[agents] - list of agents 
 * @param {string}[agentName] - agent name
 * 
 * @returns Agent
 */
export const getAgentByName = (agents: Agent[], agentName?: string) => {
    return agents.find(agent => agent.name === agentName)
}
