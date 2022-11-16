import {
   Agent,
   AgentType,
   CloudNetworkAgent,
   GreenEnergyAgent,
   MonitoringAgent,
   ServerAgent,
} from '@types'

const CLOUD_NETWORK_STATISTICS = [
   { key: 'connectedServers', label: 'Number of connected servers' },
   { key: 'maximumCapacity', label: 'Current maximum capacity' },
   { key: 'traffic', label: 'Current traffic' },
   { key: 'totalNumberOfClients', label: 'Number of clients' },
   {
      key: 'totalNumberOfExecutedJobs',
      label: 'Number of currently executed jobs',
   },
]

const SERVER_STATISTICS = [
   { key: 'isActive', label: 'Current state' },
   { key: 'initialMaximumCapacity', label: 'Maximum capacity' },
   { key: 'currentMaximumCapacity', label: 'Current maximium capacity' },
   { key: 'traffic', label: 'Current traffic' },
   { key: 'backUpTraffic', label: 'Current back-up traffic' },
   { key: 'totalNumberOfClients', label: 'Number of clients' },
   { key: 'numberOfExecutedJobs', label: 'Number of currently executed jobs' },
   { key: 'numberOfJobsOnHold', label: 'Number of jobs on-hold' },
]

const GREEN_SOURCE_STATISTICS = [
   { key: 'isActive', label: 'Current state' },
   { key: 'latitude', label: 'Location latitude' },
   { key: 'longitude', label: 'Location longitude' },
   { key: 'initialMaximumCapacity', label: 'Maximum capacity' },
   { key: 'currentMaximumCapacity', label: 'Current maximium capacity' },
   { key: 'traffic', label: 'Current traffic' },
   { key: 'numberOfExecutedJobs', label: 'Number of currently executed jobs' },
   { key: 'numberOfJobsOnHold', label: 'Number of jobs on-hold' },
]

const MONITORING_STATISTICS = [
   { key: 'greenEnergyAgent', label: 'Connected Green Energy Source' },
]

const mapCloudNetworkAgentFields = (agent: CloudNetworkAgent) => {
   const connectedServers = agent.serverAgents.length
   return { connectedServers, ...agent }
}

const mapServerAgentFields = (agent: ServerAgent) => {
   const { isActive, ...data } = agent
   const activeLabel = isActive ? 'ACTIVE' : 'INACTIVE'
   return { isActive: activeLabel, ...data }
}

const mapGreenEnergyAgentFields = (agent: GreenEnergyAgent) => {
   const { isActive, agentLocation, ...data } = agent
   const { latitude, longitude } = agentLocation
   const activeLabel = isActive ? 'ACTIVE' : 'INACTIVE'
   return { isActive: activeLabel, latitude, longitude, ...data }
}

const mapMonitoringAgentFields = (agent: MonitoringAgent) => {
   return { ...(agent as MonitoringAgent) }
}

export const getStatisticsMapForAgent = (agent: Agent) => {
   switch (agent.type) {
      case AgentType.CLOUD_NETWORK:
         return CLOUD_NETWORK_STATISTICS
      case AgentType.SERVER:
         return SERVER_STATISTICS
      case AgentType.GREEN_ENERGY:
         return GREEN_SOURCE_STATISTICS
      case AgentType.MONITORING:
         return MONITORING_STATISTICS
      default:
         return []
   }
}

export const getAgentFields = (agent: Agent) => {
   switch (agent.type) {
      case AgentType.SERVER:
         return mapServerAgentFields(agent as ServerAgent)
      case AgentType.CLOUD_NETWORK:
         return mapCloudNetworkAgentFields(agent as CloudNetworkAgent)
      case AgentType.GREEN_ENERGY:
         return mapGreenEnergyAgentFields(agent as GreenEnergyAgent)
      case AgentType.MONITORING:
         return mapMonitoringAgentFields(agent as MonitoringAgent)
   }
}
