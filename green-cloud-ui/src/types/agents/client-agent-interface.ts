import { JobStatus } from "types/agents/common/job-status-enum";
import { CommonAgentInterface } from "./common/common-agent-interface";

export interface ClientAgent extends CommonAgentInterface {
    jobId: string,
    power: string,
    startDate: string,
    endDate: string,
    jobStatusEnum: JobStatus
}