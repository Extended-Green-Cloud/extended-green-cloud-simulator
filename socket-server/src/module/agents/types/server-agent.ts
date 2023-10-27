import { ResourceMap } from "../../../types";
import { CommonAgent } from "./common-agent";

export interface ServerAgentStatic {
	cloudNetworkAgent: string;
	greenEnergyAgents: string[];
	maxPower: number;
	idlePower: number;
	resources: ResourceMap;
	price: number;
}

export interface ServerAgentDynamic {
	traffic: number;
	backUpTraffic: number;
	inUseResources: ResourceMap;
	powerConsumption: number;
	powerConsumptionBackUp: number;
	totalNumberOfClients: number;
	numberOfExecutedJobs: number;
	numberOfJobsOnHold: number;
	successRatio: number;
}

export type ServerAgent = CommonAgent & ServerAgentStatic & ServerAgentDynamic;
