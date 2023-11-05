import { AGENT_TYPES, JOB_STATUSES } from "../../../constants";
import { AgentEvent } from "../../../types";
import { Job } from "./job";

export interface JobDurationMap {
	[key: string]: number;
	CREATED: number;
	PROCESSED: number;
	IN_PROGRESS: number;
	DELAYED: number;
	FINISHED: number;
	ON_BACK_UP: number;
	ON_HOLD: number;
	REJECTED: number;
	FAILED: number;
}

export interface Client {
	type: AGENT_TYPES;
	name: string;
	events: AgentEvent[];
	isActive: boolean;
	adaptation: string;
	finalPrice: number;
	estimatedPrice: number;
	executor: string;
	job: Job;
	status: JOB_STATUSES;
	durationMap: JobDurationMap | null;
	jobExecutionProportion: number;
}
