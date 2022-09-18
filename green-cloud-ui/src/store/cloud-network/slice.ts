import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { MessagePayload, CloudNetworkStore, AgentType, CloudNetworkAgent, ClientAgent, JobStatus, CommonNetworkAgentInterface, CapacityMessage, ServerAgent, RegisterClientMessage, RegisterCloudNetworkMessage, RegisterGreenEnergyMessage, RegisterMonitoringMessage, RegisterServerMessage, RegisterAgent } from "@types";
import { calculateAgentTraffic, changeEdgeState, getAgentByName, getEdges, registerClient, registerCloudNetwork, registerGreenEnergy, registerMonitoring, registerServer } from './api';

const INITIAL_STATE: CloudNetworkStore = {
    agents: [],
    selectedAgent: undefined,
    currClientsNo: 0,
    currActiveJobsNo: 0,
    currPlannedJobsNo: 0,
    finishedJobsNo: 0,
    failedJobsNo: 0,
    totalPrice: 0
}

export const cloudNetworkSlice = createSlice({
    name: 'cloudNetwork',
    initialState: INITIAL_STATE,
    reducers: {
        setSelectedAgent(state, action: PayloadAction<string>) {
            const agent = getAgentByName(state.agents, action.payload)
            if (agent) {
                state.selectedAgent = agent
            }
        },
        displayAgentEdge(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent) {
                const targetAgents = action.payload.data as string[]
                const edges = getEdges(agent, targetAgents)
                changeEdgeState(edges, 'active')
            }
        },
        hideAgentEdge(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent) {
                const targetAgents = action.payload.data as string[]
                const edges = getEdges(agent, targetAgents)
                changeEdgeState(edges, 'inactive')
            }
        },
        registerAgent(state, action: PayloadAction<MessagePayload>) {
            if (!getAgentByName(state.agents, (action.payload.data as RegisterAgent).name)) {
                let newAgent
                switch (action.payload.agentType) {
                    case AgentType.CLIENT:
                        newAgent = registerClient(action.payload.data as RegisterClientMessage)
                        break
                    case AgentType.CLOUD_NETWORK:
                        newAgent = registerCloudNetwork(action.payload.data as RegisterCloudNetworkMessage)
                        break
                    case AgentType.GREEN_ENERGY:
                        newAgent = registerGreenEnergy(action.payload.data as RegisterGreenEnergyMessage)
                        break
                    case AgentType.MONITORING:
                        newAgent = registerMonitoring(action.payload.data as RegisterMonitoringMessage)
                        break
                    case AgentType.SERVER:
                        newAgent = registerServer(action.payload.data as RegisterServerMessage)
                        break
                }

                if (newAgent) {
                    state.agents.push(newAgent)
                }
            }
        },
        incrementFinishedJobs(state) {
            state.finishedJobsNo++
        },
        incrementFailedJobs(state) {
            state.failedJobsNo++
        },
        updateCurrentClientNumber(state, action: PayloadAction<MessagePayload>) {
            state.currClientsNo += action.payload.data as number
        },
        updateCurrentPlannedJobsNumber(state, action: PayloadAction<MessagePayload>) {
            state.currPlannedJobsNo += action.payload.data as number
        },
        updateCurrentActiveJobsNumber(state, action: PayloadAction<MessagePayload>) {
            state.currActiveJobsNo += action.payload.data as number
        },
        setTotalPrice(state, action: PayloadAction<MessagePayload>) {
            state.totalPrice = action.payload.data as number
        },
        setMaximumCapacity(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent && (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY)) {
                const networkAgent = agent as CommonNetworkAgentInterface
                const { powerInUse, maximumCapacity } = action.payload.data as CapacityMessage
                networkAgent.currentMaximumCapacity = maximumCapacity
                networkAgent.traffic = calculateAgentTraffic(networkAgent.currentMaximumCapacity, powerInUse)
            }
        },
        setTraffic(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent) {
                const powerInUse = action.payload.data as number
                if (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY) {
                    const networkAgent = agent as CommonNetworkAgentInterface
                    networkAgent.traffic = calculateAgentTraffic(networkAgent.currentMaximumCapacity, powerInUse)
                } else if (agent.type === AgentType.CLOUD_NETWORK) {
                    const cna = agent as CloudNetworkAgent
                    cna.traffic = calculateAgentTraffic(cna.maximumCapacity, powerInUse)
                }
            }
        },
        setIsActive(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent && (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY)) {
                (agent as CommonNetworkAgentInterface).isActive = action.payload.data as boolean
            }
        },
        setJobsCount(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent) {
                if (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY) {
                    (agent as CommonNetworkAgentInterface).numberOfExecutedJobs = action.payload.data as number
                } else if (agent.type === AgentType.CLOUD_NETWORK) {
                    (agent as CloudNetworkAgent).totalNumberOfExecutedJobs = action.payload.data as number
                }
            }
        },
        setOnHoldJobsCount(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent && (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY)) {
                (agent as CommonNetworkAgentInterface).numberOfJobsOnHold = action.payload.data as number
            }
        },
        setClientNumber(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent && agent.type === AgentType.CLOUD_NETWORK) {
                (agent as CloudNetworkAgent).totalNumberOfClients = action.payload.data as number
            } else if (agent && agent.type === AgentType.SERVER) {
                (agent as ServerAgent).totalNumberOfClients = action.payload.data as number
            }
        },
        setClientJobStatus(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent && agent.type === AgentType.CLIENT) {
                (agent as ClientAgent).jobStatusEnum = action.payload.data as JobStatus
            }
        },
        setServerBackUpTraffic(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            if (agent && agent.type === AgentType.SERVER) {
                const server = agent as ServerAgent
                const backUp = action.payload.data as number
                server.backUpTraffic = calculateAgentTraffic(server.initialMaximumCapacity, backUp)
            }
        }
    }
})