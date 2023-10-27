import {
   Agent,
   AgentType,
   CloudNetworkAgent,
   PowerShortageEventState,
   GreenEnergyAgent,
   MonitoringAgent,
   MultiLevelDetails,
   ServerAgent,
   EventType,
   SwitchOnOffEvent
} from '@types'
import { getEventByType } from '@utils'
import { PowerShortageEvent } from 'types/event/power-shortage-event'
import { collectResourcesToMultiMap, mapInUseValues } from 'utils/resource-utils'

const CLOUD_NETWORK_STATISTICS_STATE = [
   { key: 'connectedServers', label: 'Number of connected servers' },
   { key: 'totalNumberOfClients', label: 'Number of clients' },
   {
      key: 'totalNumberOfExecutedJobs',
      label: 'Number of currently executed job instances'
   }
]

const CLOUD_NETWORK_STATISTICS_QUALITY = [
   { key: 'traffic', label: 'Current traffic' },
   {
      key: 'successRatio',
      label: 'Current job execution success ratio'
   }
]

const SERVER_STATISTICS_RESOURCE_CHARACTERISTICS = [{ label: 'In use', mapper: mapInUseValues }]

const SERVER_STATISTICS_RESOURCES = [{ key: 'resourceMap', label: '-' }]

const SERVER_STATISTICS_VALUATION = [{ key: 'price', label: 'Power price (per kWh)' }]

const SERVER_STATISTICS_POWER = [
   { key: 'powerConsumed', label: 'Current power consumption (W)' },
   { key: 'idlePower', label: 'Idle power consumption (W)' },
   { key: 'maxPower', label: 'Maximal possible power consumption (W)' }
]

const SERVER_STATISTICS_STATE = [
   { key: 'isActive', label: 'Current state' },
   { key: 'totalNumberOfClients', label: 'Number of planned job instances' },
   {
      key: 'numberOfExecutedJobs',
      label: 'Number of currently executed job instances'
   },
   { key: 'numberOfJobsOnHold', label: 'Number of job instances on-hold' }
]

const SERVER_STATISTICS_QUALITY = [
   { key: 'traffic', label: 'Current traffic' },
   { key: 'backUpTraffic', label: 'Current back-up traffic' },
   {
      key: 'successRatio',
      label: 'Current job execution success ratio'
   }
]

const GREEN_SOURCE_STATISTICS_ENERGY = [
   { key: 'pricePerPower', label: 'Energy price (for kWh)' },
   { key: 'maximumCapacity', label: 'Generator maximum capacity (kWh)' },
   { key: 'energy', label: 'Produced energy (kWh)' }
]

const GREEN_SOURCE_STATISTICS_STATE = [
   { key: 'isActive', label: 'Current state' },
   { key: 'latitude', label: 'Location latitude' },
   { key: 'longitude', label: 'Location longitude' },
   { key: 'numberOfExecutedJobs', label: 'Number of currently executed jobs' },
   { key: 'numberOfJobsOnHold', label: 'Number of jobs on-hold' }
]

const GREEN_SOURCE_STATISTICS_QUALITY = [
   { key: 'traffic', label: 'Current traffic' },
   {
      key: 'successRatio',
      label: 'Current job execution success ratio'
   },
   { key: 'weatherPredictionError', label: 'Current weather prediction error' }
]

const MONITORING_STATISTICS = [{ key: 'greenEnergyAgent', label: 'Connected Green Energy Source' }]

const mapCloudNetworkAgentFields = (agent: CloudNetworkAgent) => {
   const connectedServers = agent.serverAgents.length
   return { connectedServers, ...agent }
}

const mapServerAgentFields = (agent: ServerAgent) => {
   const { isActive, powerConsumption, resources, inUseResources, ...data } = agent
   const powerShortageEvent = getEventByType(EventType.POWER_SHORTAGE_EVENT, agent) as PowerShortageEvent
   const switchOffServer = getEventByType(EventType.SWITCH_ON_OFF_EVENT, agent) as SwitchOnOffEvent

   let activeLabel = StateTypes.INACTIVE

   if (powerShortageEvent.state === PowerShortageEventState.INACTIVE) {
      activeLabel = StateTypes.BROKEN
   } else if (!switchOffServer.isServerOn) {
      activeLabel = StateTypes.DISABLED
   } else if (isActive) {
      activeLabel = StateTypes.ACTIVE
   }

   const currPowerConsumption = powerConsumption.toFixed(2)
   const resourceMap: MultiLevelDetails[] = collectResourcesToMultiMap(
      resources,
      SERVER_STATISTICS_RESOURCE_CHARACTERISTICS,
      inUseResources
   )
   return {
      isActive: activeLabel,
      resourceMap,
      powerConsumed: currPowerConsumption,
      ...data
   }
}

export enum StateTypes {
   BROKEN = 'BROKEN',
   DISABLED = 'DISABLED',
   ACTIVE = 'ACTIVE',
   INACTIVE = 'INACTIVE'
}
type BadgeColors = { [key in StateTypes]: string }

export const BADGE_STATE_COLORS: BadgeColors = {
   [StateTypes.ACTIVE]: 'var(--green-1)',
   [StateTypes.INACTIVE]: 'var(--gray-2)',
   [StateTypes.BROKEN]: 'var(--red-1)',
   [StateTypes.DISABLED]: 'var(--red-1)'
}

const mapGreenEnergyAgentFields = (agent: GreenEnergyAgent) => {
   const { isActive, agentLocation, ...data } = agent
   const { latitude, longitude } = agentLocation
   const energy = `${data.energyInUse.toFixed(2)} / ${data.availableGreenEnergy.toFixed(2)}`
   const powerShortageEvent = getEventByType(EventType.POWER_SHORTAGE_EVENT, agent) as PowerShortageEvent
   const activeLabel =
      powerShortageEvent.state === PowerShortageEventState.INACTIVE
         ? StateTypes.BROKEN
         : isActive
         ? StateTypes.ACTIVE
         : StateTypes.INACTIVE
   return { isActive: activeLabel, latitude, longitude, energy, ...data }
}

const mapMonitoringAgentFields = (agent: MonitoringAgent) => {
   return { ...(agent as MonitoringAgent) }
}

export const MAP_TYPE = {
   QUALITY: 'QUALITY',
   STATE: 'STATE',
   RESOURCES: 'RESOURCES',
   VALUATION: 'VALUATION',
   POWER: 'POWER',
   ENERGY: 'ENERGY'
}

export const getStatisticsMapForAgent = (agent: Agent, type?: string) => {
   switch (agent.type) {
      case AgentType.CLOUD_NETWORK:
         return type === MAP_TYPE.QUALITY ? CLOUD_NETWORK_STATISTICS_QUALITY : CLOUD_NETWORK_STATISTICS_STATE
      case AgentType.SERVER:
         if (type === MAP_TYPE.QUALITY) return SERVER_STATISTICS_QUALITY
         if (type === MAP_TYPE.VALUATION) return SERVER_STATISTICS_VALUATION
         if (type === MAP_TYPE.POWER) return SERVER_STATISTICS_POWER
         if (type === MAP_TYPE.RESOURCES) return SERVER_STATISTICS_RESOURCES
         return SERVER_STATISTICS_STATE
      case AgentType.GREEN_ENERGY:
         if (type === MAP_TYPE.QUALITY) return GREEN_SOURCE_STATISTICS_QUALITY
         if (type === MAP_TYPE.STATE) return GREEN_SOURCE_STATISTICS_STATE
         return GREEN_SOURCE_STATISTICS_ENERGY
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

export const NETWORK_AGENTS = [AgentType.CLOUD_NETWORK, AgentType.SERVER, AgentType.GREEN_ENERGY]

export const PERCENTAGE_VALUES = ['traffic', 'backUpTraffic', 'successRatio', 'weatherPredictionError']

type AgentsMaps = { [key in AgentType]?: string[] }

export const MAPS_FOR_AGENT_TYPE: AgentsMaps = {
   [AgentType.CLOUD_NETWORK]: [MAP_TYPE.STATE, MAP_TYPE.QUALITY],
   [AgentType.SERVER]: [MAP_TYPE.STATE, MAP_TYPE.RESOURCES, MAP_TYPE.VALUATION, MAP_TYPE.POWER, MAP_TYPE.QUALITY],
   [AgentType.GREEN_ENERGY]: [MAP_TYPE.STATE, MAP_TYPE.ENERGY, MAP_TYPE.QUALITY],
   [AgentType.MONITORING]: [MAP_TYPE.STATE]
}
