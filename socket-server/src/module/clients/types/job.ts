export interface JobStep {
	name: string;
	cpu: number;
	memory: number;
	duration: number;
}

export interface Job {
	jobId: string;
	processorName: string;
	cpu: number;
	memory: number;
	storage: number;
	start: string;
	end: string;
	deadline: string;
	duration: string;
	steps: JobStep[];
}
