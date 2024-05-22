import { ResourceMap } from "../../../types";

export interface JobStep {
	name: string;
	resources: ResourceMap;
	duration: number;
}

export interface Job {
	jobId: string;
	processorName: string;
	resources: ResourceMap;
	deadline: string;
	duration: number;
	steps: JobStep[];
}
