import {
   Agent,
   AgentType,
   CloudNetworkAgent,
   GreenEnergyAgent,
   MonitoringAgent,
   ServerAgent,
} from '@types'

const CLOUD_NETWORK_STATISTICS_STATE = [
   { key: 'connectedServers', label: 'Number of connected servers' },
   { key: 'maximumCapacity', label: 'Current maximum capacity' },
   { key: 'totalNumberOfClients', label: 'Number of clients' },
   {
      key: 'totalNumberOfExecutedJobs',
      label: 'Number of currently executed job instances',
   },
]

const CLOUD_NETWORK_STATISTICS_QUALITY = [
   { key: 'traffic', label: 'Current traffic' },
   {
      key: 'successRatio',
      label: 'Current job execution success ratio',
   },
]

const SERVER_STATISTICS_STATE = [
   { key: 'isActive', label: 'Current state' },
   { key: 'initialMaximumCapacity', label: 'Maximum capacity' },
   { key: 'currentMaximumCapacity', label: 'Current maximium capacity' },
   { key: 'totalNumberOfClients', label: 'Number of planned job instances' },
   {
      key: 'numberOfExecutedJobs',
      label: 'Number of currently executed job instances',
   },
   { key: 'numberOfJobsOnHold', label: 'Number of job instances on-hold' },
]

const SERVER_STATISTICS_QUALITY = [
   { key: 'traffic', label: 'Current traffic' },
   { key: 'backUpTraffic', label: 'Current back-up traffic' },
   {
      key: 'successRatio',
      label: 'Current job execution success ratio',
   },
]

const GREEN_SOURCE_STATISTICS_STATE = [
   { key: 'isActive', label: 'Current state' },
   { key: 'latitude', label: 'Location latitude' },
   { key: 'longitude', label: 'Location longitude' },
   { key: 'initialMaximumCapacity', label: 'Maximum capacity' },
   { key: 'currentMaximumCapacity', label: 'Current maximium capacity' },
   { key: 'numberOfExecutedJobs', label: 'Number of currently executed jobs' },
   { key: 'numberOfJobsOnHold', label: 'Number of jobs on-hold' },
]

const GREEN_SOURCE_STATISTICS_QUALITY = [
   { key: 'traffic', label: 'Current traffic' },
   {
      key: 'successRatio',
      label: 'Current job execution success ratio',
   },
   { key: 'weatherPredictionError', label: 'Current weather prediction error' },
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

export const MAP_TYPE = {
   QUALITY: 'QUALITY',
   STATE: 'STATE',
}

export const getStatisticsMapForAgent = (agent: Agent, type?: string) => {
   switch (agent.type) {
      case AgentType.CLOUD_NETWORK:
         return type === MAP_TYPE.QUALITY
            ? CLOUD_NETWORK_STATISTICS_QUALITY
            : CLOUD_NETWORK_STATISTICS_STATE
      case AgentType.SERVER:
         return type === MAP_TYPE.QUALITY
            ? SERVER_STATISTICS_QUALITY
            : SERVER_STATISTICS_STATE
      case AgentType.GREEN_ENERGY:
         return type === MAP_TYPE.QUALITY
            ? GREEN_SOURCE_STATISTICS_QUALITY
            : GREEN_SOURCE_STATISTICS_STATE
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

export const PERCENTAGE_VALUES = [
   'traffic',
   'backUpTraffic',
   'successRatio',
   'weatherPredictionError',
]
