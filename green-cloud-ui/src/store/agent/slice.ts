import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { MessagePayload, AgentType, CloudNetworkAgent, ClientAgent, JobStatus, CommonNetworkAgentInterface, CapacityMessage, ServerAgent, RegisterAgent, PowerShortageEventData, EventType, EventState, PowerShortageMessage, AgentStore } from "@types";
import { createEdgesForAgent } from '@utils';
import { sendMessageUsnigSocket } from 'store/socket';
import { changeAgentActivness, changeAgentBackUpTraffic, changeAgentJobsOnHold, changeAgentMaximumCapacity, changeAgentTraffic, getAgentByName, getEventByType,  registerNewAgent } from './api';

const INITIAL_STATE: AgentStore = {
    agents: [],
    connections: [],
    selectedAgent: null
}

export const agentSlice = createSlice({
    name: 'agents',
    initialState: INITIAL_STATE,
    reducers: {
        setPowerShortageCapacity(state, action: PayloadAction<PowerShortageEventData>) {
            const agent = getAgentByName(state.agents, action.payload.agentName)
            if (agent) {
                const event = getEventByType(agent.events, EventType.POWER_SHORTAGE_EVENT)
                if (event) {
                    const capacity = action.payload.newMaximumCapacity
                    event!.data = { newMaximumCapacity: capacity };
                }
            }
        },
        triggerPowerShortage(state, action: PayloadAction<string | undefined>) {
            const agent = getAgentByName(state.agents, action.payload)
            if (agent) {
                const event = getEventByType(agent.events, EventType.POWER_SHORTAGE_EVENT)
                if (event) {
                    const networkAgent = agent as CommonNetworkAgentInterface
                    networkAgent.currentMaximumCapacity = EventState.ACTIVE === event.state ?
                        event.data?.newMaximumCapacity as number :
                        networkAgent.initialMaximumCapacity
                    const occurenceTime = new Date()
                    occurenceTime.setSeconds(occurenceTime.getSeconds() + 2)
                    event!.occurenceTime = occurenceTime.toJSON()
                    event.disabled = true
                    event.state = EventState.ACTIVE === event.state ? EventState.INACTIVE : EventState.ACTIVE;
                    const data: PowerShortageMessage = { ...event, agentName: agent.name }
                    sendMessageUsnigSocket(JSON.stringify(data))
                    event.data = null
                }
            }
        },
        unlockPowerShortageEvent(state, action: PayloadAction<string | undefined>) {
            const agent = getAgentByName(state.agents, action.payload)
            if (agent) {
                const event = getEventByType(agent.events, EventType.POWER_SHORTAGE_EVENT)
                if (event) {
                    event.disabled = false
                }
            }
        },
        // displayAgentEdge(state, action: PayloadAction<MessagePayload>) {
        //     const sourceName = action.payload?.agentName
        //     const targetAgents = action.payload.data as string[]
        //     if (sourceName && targetAgents) {
        //         changeAgentEdgeState(sourceName, targetAgents, 'active')
        //     }
        // },
        // hideAgentEdge(state, action: PayloadAction<MessagePayload>) {
        //     const sourceName = action.payload?.agentName
        //     const targetAgents = action.payload.data as string[]
        //     if (sourceName && targetAgents) {
        //         changeAgentEdgeState(sourceName, targetAgents, 'inactive')
        //     }
        // },
        registerAgent(state, action: PayloadAction<MessagePayload>) {
            const registerMsg = action.payload.data as RegisterAgent
            const type = action.payload.agentType as AgentType

            if (!getAgentByName(state.agents, registerMsg.name)) {
                let newAgent = registerNewAgent(registerMsg, type)
                if (newAgent) {
                    state.agents.push(newAgent)
                    state.connections = state.connections.concat(createEdgesForAgent(newAgent))
                }
            }
        },
        setMaximumCapacity(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            const { powerInUse, maximumCapacity } = action.payload.data as CapacityMessage

            if (agent && (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY)) {
                changeAgentMaximumCapacity(agent as CommonNetworkAgentInterface, powerInUse, maximumCapacity)
            }
        },
        setTraffic(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)

            if (agent) {
                const powerInUse = action.payload.data as number
                changeAgentTraffic(agent, powerInUse)
            }
        },
        setIsActive(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            const isActive = action.payload.data as boolean

            if (agent && (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY)) {
                changeAgentActivness(agent as CommonNetworkAgentInterface, isActive)
            }
        },
        setJobsCount(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            const jobsCount = action.payload.data as number

            if (agent) {
                if (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY) {
                    (agent as CommonNetworkAgentInterface).numberOfExecutedJobs = jobsCount
                } else if (agent.type === AgentType.CLOUD_NETWORK) {
                    (agent as CloudNetworkAgent).totalNumberOfExecutedJobs = jobsCount
                }
            }
        },
        setOnHoldJobsCount(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)
            const jobsOnHold = action.payload.data as number

            if (agent && (agent.type === AgentType.SERVER || agent?.type === AgentType.GREEN_ENERGY)) {
                changeAgentJobsOnHold(agent as CommonNetworkAgentInterface, jobsOnHold)
            }
        },
        setClientNumber(state, action: PayloadAction<MessagePayload>) {
            const agent = getAgentByName(state.agents, action.payload?.agentName)

            if (agent && (agent.type === AgentType.CLOUD_NETWORK || agent.type === AgentType.CLOUD_NETWORK)) {
                const typedAgent = agent as CloudNetworkAgent | ServerAgent
                typedAgent.totalNumberOfClients = action.payload.data as number
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
            const backUp = action.payload.data as number

            if (agent && agent.type === AgentType.SERVER) {
                changeAgentBackUpTraffic(agent as ServerAgent, backUp)
            }
        },
        setSelectedAgent(state, action: PayloadAction<string>) {
            state.selectedAgent = action.payload
        },
        resetAgents(state) {
            Object.assign(state, INITIAL_STATE)
        }
    }
})