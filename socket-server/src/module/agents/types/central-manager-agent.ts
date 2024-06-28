import { CommonAgent } from "./common-agent";

export interface ScheduledJob {
	clientName: string;
	jobId: string;
}

export interface CentralManagerAgentStatic {
	maxQueueSize: number;
}

export interface CentralManagerAgentDynamic {
	scheduledJobs: ScheduledJob[];
}

export type CentralManagerAgent = CommonAgent & CentralManagerAgentStatic & CentralManagerAgentDynamic;
