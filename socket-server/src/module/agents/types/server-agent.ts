import { CommonAgent } from "./common-agent";

export interface ServerAgentStatic {
	cloudNetworkAgent: string;
	greenEnergyAgents: string[];
	maxPower: number;
	idlePower: number;
	cpu: number;
	memory: number;
	storage: number;
	price: number;
}

export interface ServerAgentDynamic {
	traffic: number;
	backUpTraffic: number;
	inUseCpu: number;
	inUseMemory: number;
	inUseStorage: number;
	powerConsumption: number;
	powerConsumptionBackUp: number;
	totalNumberOfClients: number;
	numberOfExecutedJobs: number;
	numberOfJobsOnHold: number;
	successRatio: number;
}

export type ServerAgent = CommonAgent & ServerAgentStatic & ServerAgentDynamic;
