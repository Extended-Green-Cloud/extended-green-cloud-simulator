import { CommonAgent } from "./common-agent";

export interface ScheduledJob {
	clientName: string;
	jobId: string;
}

export interface SchedulerAgentStatic {
	maxQueueSize: number;
}

export interface SchedulerAgentDynamic {
	scheduledJobs: ScheduledJob[];
	deadlinePriority: number;
	cpuPriority: number;
}

export type SchedulerAgent = CommonAgent & SchedulerAgentStatic & SchedulerAgentDynamic;
