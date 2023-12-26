import { ResourceMap } from "../../../types";
import { CommonAgent } from "./common-agent";

export interface RegionalManagerAgentStatic {
	serverAgents: string[];
	resources: ResourceMap;
}

export interface RegionalManagerAgentDynamic {
	maxCpuInServers: number;
	traffic: number;
	totalNumberOfClients: number;
	totalNumberOfExecutedJobs: number;
	successRatio: number;
	inUseResources: ResourceMap;
}

export type RegionalManagerAgent = CommonAgent & RegionalManagerAgentStatic & RegionalManagerAgentDynamic;
