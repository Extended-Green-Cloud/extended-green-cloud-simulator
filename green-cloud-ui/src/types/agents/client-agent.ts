import { JobStatus } from "types/enum/job-status-enum";
import { CommonAgentInterface } from "./common/common-agent";

export interface ClientAgent extends CommonAgentInterface {
    jobId: string,
    power: string,
    startDate: string,
    endDate: string,
    jobStatusEnum: JobStatus
}