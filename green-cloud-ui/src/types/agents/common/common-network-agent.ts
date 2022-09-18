import { CommonAgentInterface } from "./common-agent";

export interface CommonNetworkAgentInterface extends CommonAgentInterface {
    initialMaximumCapacity: number,
    currentMaximumCapacity: number,
    isActive: boolean,
    traffic: number,
    numberOfExecutedJobs: number,
    numberOfJobsOnHold: number
}