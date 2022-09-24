import { CommonAgentInterface } from "./common-agent";

export interface CommonNetworkAgentInterface extends CommonAgentInterface {
    initialMaximumCapacity: number,
    currentMaximumCapacity: number,
    traffic: number,
    numberOfExecutedJobs: number,
    numberOfJobsOnHold: number
}

export const DEFAULT_NETWORK_AGENT_START_COMMONS = (data: any) => {
    return ({
        initialMaximumCapacity: data.maximumCapacity,
        currentMaximumCapacity: data.maximumCapacity,
        traffic: 0,
        numberOfExecutedJobs: 0,
        numberOfJobsOnHold: 0,
    })
}