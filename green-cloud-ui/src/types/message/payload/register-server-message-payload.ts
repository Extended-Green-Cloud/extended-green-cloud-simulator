export type RegisterServerMessage = {
    name: string,
    maximumCapacity: number,
    cloudNetworkAgent: string,
    greenEnergyAgents: string[]
}