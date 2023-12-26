import {
	INITIAL_POWER_SHORTAGE_STATE,
	INITIAL_SERVER_MAINTENANCE_STATE,
	INITIAL_SWITCH_ON_OFF_STATE,
	INITIAL_WEATHER_DROP_STATE,
	JOB_STATUSES,
} from "../constants/constants";
import { AGENT_TYPES } from "../constants/constants";
import { AGENTS_REPORTS_STATE, Client, AGENTS_STATE } from "../module";
import { changeCloudNetworkCapacityEvent } from "../module/agents/report-handlers/report-handler";
import { CloudNetworkAgent, GreenEnergyAgent, SchedulerAgent } from "../module/agents/types";
import { ServerAgent } from "../module/agents/types/server-agent";
import { Resource, ResourceCharacteristic, ResourceMap } from "../types";

const getAgentByName = (agents: any[], agentName: string) => {
	return agents.find((agent) => agent.name === agentName);
};

const getAgentsByName = (agents: any[], agentNames: string[]) => {
	return agents.filter((agent) => agentNames.includes(agent.name));
};

const getAgentNodeById = (nodes: any[], id: string) => {
	return nodes.find((node) => node.id === id);
};

type ResourceMapEntries = { [key: string]: Resource };

const mapServerResources = (resources: ResourceMapEntries): ResourceMap => {
	return Object.entries(resources).reduce((prev, [key, resource]) => {
		const characteristicsVal = Object.entries(resource?.characteristics).reduce(
			(prevC, [keyC, resourceC]) => ({
				[keyC]: {
					unit: resourceC.unit,
					value: resourceC.value,
					toCommonUnitConverter: resourceC.toCommonUnitConverter,
					fromCommonUnitConverter: resourceC.fromCommonUnitConverter,
					resourceCharacteristicReservation: resourceC.resourceCharacteristicReservation,
					resourceCharacteristicSubtraction: resourceC.resourceCharacteristicSubtraction,
					resourceCharacteristicAddition: resourceC.resourceCharacteristicAddition,
				} as ResourceCharacteristic,
				...prevC,
			}),
			{}
		);
		return {
			...prev,
			[key]: {
				characteristics: characteristicsVal,
				emptyResource: resource.emptyResource,
				resourceValidator: resource.resourceValidator,
				resourceComparator: resource.resourceComparator,
			} as Resource,
		};
	}, {});
};

const addGreenSourcesToServer = (data) => {
	AGENTS_STATE.agents
		.filter(
			(el) =>
				el.type === AGENT_TYPES.SERVER &&
				el.name === data.serverAgent &&
				!(el as ServerAgent).greenEnergyAgents.includes(data.name)
		)
		.forEach((server: ServerAgent) => server.greenEnergyAgents.push(data.name));
};

const addServersToCNA = (data) => {
	AGENTS_STATE.agents
		.filter(
			(el) =>
				el.type === AGENT_TYPES.CLOUD_NETWORK &&
				el.name === data.cloudNetworkAgent &&
				!(el as CloudNetworkAgent).serverAgents.includes(data.name)
		)
		.forEach((cna: CloudNetworkAgent) => {
			cna.maxCpuInServers += data.cpu;
			cna.serverAgents.push(data.name);
		});
};

const registerClient = (data): Client => {
	const { name, ...jobData } = data;

	return {
		type: AGENT_TYPES.CLIENT,
		status: JOB_STATUSES.CREATED,
		executor: "",
		events: [],
		name,
		finalPrice: 0,
		estimatedPrice: 0,
		isActive: false,
		adaptation: "inactive",
		durationMap: null,
		job: jobData,
		jobExecutionProportion: 0,
	};
};

const registerScheduler = (data): SchedulerAgent => {
	AGENTS_REPORTS_STATE.agentsReports.push({
		name: data.name,
		type: AGENT_TYPES.SCHEDULER,
		reports: {
			deadlinePriorityReport: [],
			cpuPriorityReport: [],
			clientRequestReport: [],
			queueCapacityReport: [],
			trafficReport: [],
		},
		events: [],
	});
	return {
		type: AGENT_TYPES.SCHEDULER,
		scheduledJobs: [],
		events: [],
		isActive: true,
		adaptation: "inactive",
		...data,
	};
};

const registerCloudNetwork = (data): CloudNetworkAgent => {
	AGENTS_REPORTS_STATE.agentsReports.push({
		name: data.name,
		type: AGENT_TYPES.CLOUD_NETWORK,
		reports: {
			clientsReport: [],
			trafficReport: [],
			successRatioReport: [],
		},
		events: [],
	});

	return {
		type: AGENT_TYPES.CLOUD_NETWORK,
		events: [structuredClone(INITIAL_WEATHER_DROP_STATE)],
		isActive: false,
		adaptation: "inactive",
		totalNumberOfClients: 0,
		totalNumberOfExecutedJobs: 0,
		maxCpuInServers: data.maxServerCpu,
		traffic: 0,
		successRatio: 0,
		resources: {},
		inUseResources: {},
		...data,
	};
};

const registerServer = (data): ServerAgent => {
	const { emptyResources, resources, ...remainingData } = data;
	const events = [
		structuredClone(INITIAL_POWER_SHORTAGE_STATE),
		structuredClone(INITIAL_SWITCH_ON_OFF_STATE),
		structuredClone(INITIAL_SERVER_MAINTENANCE_STATE),
	];

	AGENTS_REPORTS_STATE.agentsReports.push({
		name: remainingData.name,
		type: AGENT_TYPES.SERVER,
		reports: {
			trafficReport: [],
			cpuInUseReport: [],
			powerConsumptionReport: [],
			backUpPowerConsumptionReport: [],
			successRatioReport: [],
		},
		events: [],
	});

	addServersToCNA(remainingData);
	changeCloudNetworkCapacityEvent(
		remainingData.cloudNetworkAgent,
		remainingData.name,
		remainingData.initialMaximumCapacity,
		true,
		true
	);

	return {
		type: AGENT_TYPES.SERVER,
		totalNumberOfClients: 0,
		events,
		isActive: false,
		adaptation: "inactive",
		traffic: 0,
		backUpTraffic: 0,
		inUseResources: mapServerResources(emptyResources),
		powerConsumption: 0,
		powerConsumptionBackUp: 0,
		numberOfClients: 0,
		numberOfExecutedJobs: 0,
		numberOfJobsOnHold: 0,
		successRatio: 0,
		resources: mapServerResources(resources),
		...remainingData,
	};
};

const registerGreenEnergy = (data): GreenEnergyAgent => {
	const events = [structuredClone(INITIAL_POWER_SHORTAGE_STATE)];

	AGENTS_REPORTS_STATE.agentsReports.push({
		name: data.name,
		type: AGENT_TYPES.GREEN_ENERGY,
		reports: {
			trafficReport: [],
			availableGreenPowerReport: [],
			energyInUseReport: [],
			jobsOnGreenPowerReport: [],
			jobsOnHoldReport: [],
			successRatioReport: [],
		},
		events: [],
	});

	addGreenSourcesToServer(data);

	return {
		type: AGENT_TYPES.GREEN_ENERGY,
		events,
		isActive: false,
		adaptation: "inactive",
		connectedServers: [data.serverAgent],
		traffic: 0,
		energyInUse: 0,
		numberOfExecutedJobs: 0,
		numberOfJobsOnHold: 0,
		successRatio: 0,
		availableGreenEnergy: 0,
		...data,
	};
};

const registerMonitoring = (data) => {
	return {
		type: AGENT_TYPES.MONITORING,
		events: [],
		isActive: false,
		adaptation: "inactive",
		...data,
	};
};

const registerAgent = (data, type) => {
	switch (type) {
		case AGENT_TYPES.CLIENT:
			return registerClient(data);
		case AGENT_TYPES.CLOUD_NETWORK:
			return registerCloudNetwork(data);
		case AGENT_TYPES.GREEN_ENERGY:
			return registerGreenEnergy(data);
		`case AGENT_TYPES.MONITORING:
			return registerMonitoring(data);
		case AGENT_TYPES.SERVER:
			return registerServer(data);
		case AGENT_TYPES.SCHEDULER:
			return registerScheduler(data);
	}
};

export {
	getAgentByName,
	getAgentsByName,
	getAgentNodeById,
	registerClient,
	registerScheduler,
	registerCloudNetwork,
	registerGreenEnergy,
	registerServer,
	registerMonitoring,
	registerAgent,
	mapServerResources,
};
