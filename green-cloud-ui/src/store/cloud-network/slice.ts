import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { CloudNetwork, AgentNodeInterface, AgentType, CloudNetworkAgent } from "@types";
import { TrafficPayload } from 'types/store/payload/traffic-payload-interface';

const MOCK_AGENTS: AgentNodeInterface[] = [
    { type: AgentType.CLOUD_NETWORK, name: 'CNA1', serverAgents: ['Server1', 'Server2'], maximumCapacity: 0, traffic: 0, totalNumberOfClients: 0, totalNumberOfExecutedJobs: 0 },
    { type: AgentType.CLOUD_NETWORK, name: 'CNA2', serverAgents: ['Server3', 'Server4'], maximumCapacity: 0, traffic: 0, totalNumberOfClients: 0, totalNumberOfExecutedJobs: 0 },
    { type: AgentType.SERVER, name: 'Server1', greenEnergyAgents: ['Solar1', 'Water1'], cloudNetworkAgent: 'CNA1', traffic: 0, backUpTraffic: 0, totalNumberOfClients: 0, initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false },
    { type: AgentType.SERVER, name: 'Server2', greenEnergyAgents: ['Solar2', 'Water2'], cloudNetworkAgent: 'CNA1', traffic: 0, backUpTraffic: 0, totalNumberOfClients: 0, initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false },
    { type: AgentType.SERVER, name: 'Server3', greenEnergyAgents: ['Solar3', 'Water3'], cloudNetworkAgent: 'CNA2', traffic: 0, backUpTraffic: 0, totalNumberOfClients: 0, initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false },
    { type: AgentType.SERVER, name: 'Server4', greenEnergyAgents: ['Solar4', 'Water4'], cloudNetworkAgent: 'CNA2', traffic: 0, backUpTraffic: 0, totalNumberOfClients: 0, initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false },
    { type: AgentType.GREEN_ENERGY, name: 'Solar1', monitoringAgent: 'Weather1', traffic: 0, serverAgent: 'Server1', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Solar2', monitoringAgent: 'Weather2', traffic: 0, serverAgent: 'Server2', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Solar3', monitoringAgent: 'Weather3', traffic: 0, serverAgent: 'Server3', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Solar4', monitoringAgent: 'Weather4', traffic: 0, serverAgent: 'Server4', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Water1', monitoringAgent: 'Weather5', traffic: 0, serverAgent: 'Server1', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Water2', monitoringAgent: 'Weather6', traffic: 0, serverAgent: 'Server2', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Water3', monitoringAgent: 'Weather7', traffic: 0, serverAgent: 'Server3', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.GREEN_ENERGY, name: 'Water4', monitoringAgent: 'Weather8', traffic: 0, serverAgent: 'Server4', initialMaximumCapacity: 0, currentMaximumCapacity: 0, numberOfExecutedJobs: 0, numberOfJobsOnHold: 0, isActive: false, agentLocation: { latitude: '11', longitude: '10' } },
    { type: AgentType.MONITORING, name: 'Weather1', greenEnergyAgent: 'Solar1'},
    { type: AgentType.MONITORING, name: 'Weather2', greenEnergyAgent: 'Solar2'},
    { type: AgentType.MONITORING, name: 'Weather3', greenEnergyAgent: 'Solar3'},
    { type: AgentType.MONITORING, name: 'Weather4', greenEnergyAgent: 'Solar4'},
    { type: AgentType.MONITORING, name: 'Weather5', greenEnergyAgent: 'Water1'},
    { type: AgentType.MONITORING, name: 'Weather6', greenEnergyAgent: 'Water2'},
    { type: AgentType.MONITORING, name: 'Weather7', greenEnergyAgent: 'Water3'},
    { type: AgentType.MONITORING, name: 'Weather8', greenEnergyAgent: 'Water4'}
]

const INITIAL_STATE: CloudNetwork = {
    agents: MOCK_AGENTS,
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
        setTotalPrice(state, action: PayloadAction<number>) {
            state.totalPrice = action.payload
        },
        incrementPrice(state) {
            state.currActiveJobsNo = state.currActiveJobsNo + 1
        },
        setAgentTraffic(state, action: PayloadAction<TrafficPayload>) {
            const agent = state.agents.find(agent => agent.name === action.payload.agentName)
            if(agent && agent.type === AgentType.CLOUD_NETWORK) {
                (agent as CloudNetworkAgent).traffic = action.payload.traffic
            }
        }
    }
})