import { CommonAgent } from "./common-agent";

export interface CloudNetworkAgentStatic {
	serverAgents: string[];
}

export interface CloudNetworkAgentDynamic {
	maxCpuInServers: number;
	traffic: number;
	totalNumberOfClients: number;
	totalNumberOfExecutedJobs: number;
	successRatio: number;
}

export type CloudNetworkAgent = CommonAgent & CloudNetworkAgentStatic & CloudNetworkAgentDynamic;
