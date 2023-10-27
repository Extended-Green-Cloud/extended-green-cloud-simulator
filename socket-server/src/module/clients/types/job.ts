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
	start: string;
	end: string;
	deadline: string;
	duration: string;
	steps: JobStep[];
}
