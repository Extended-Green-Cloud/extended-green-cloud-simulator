import { ResourceMap } from "../../../types";
import { CommonAgent } from "./common-agent";

export interface CloudNetworkAgentStatic {
	serverAgents: string[];
	resources: ResourceMap;
}

export interface CloudNetworkAgentDynamic {
	maxCpuInServers: number;
	traffic: number;
	totalNumberOfClients: number;
	totalNumberOfExecutedJobs: number;
	successRatio: number;
	inUseResources: ResourceMap;
}

export type CloudNetworkAgent = CommonAgent & CloudNetworkAgentStatic & CloudNetworkAgentDynamic;
