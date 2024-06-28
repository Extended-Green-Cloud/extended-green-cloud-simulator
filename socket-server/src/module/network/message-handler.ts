import { NETWORK_STATE } from "./network-state";

const handleIncrementFinishJobs = (msg) => (NETWORK_STATE.finishedJobsNo += msg.data);
const handleIncrementFinishInCloudJobs = (msg) => (NETWORK_STATE.finishedJobsInCloudNo += msg.data);
const handleIncrementFailedJobs = (msg) => (NETWORK_STATE.failedJobsNo += msg.data);
const handlePlannedJobs = (msg) => (NETWORK_STATE.currPlannedJobsNo += msg.data);
const handleExecutedJobs = (msg) => (NETWORK_STATE.currActiveJobsNo += msg.data);
const handleExecutedInCloudJobs = (msg) => (NETWORK_STATE.currActiveInCloudJobsNo += msg.data);
const handleCurrentClientsNumber = (msg) => (NETWORK_STATE.currClientsNo += msg.data);
const handleUpdateAllocationAcceptance = (msg) => (NETWORK_STATE.avgAllocationAcceptanceRatio = msg.data);

const handleUpdateStrategyProperties = (msg) => {
	const { allocationName, prioritizationName, allocationStepsNo, modificationList } = msg;

	NETWORK_STATE.allocationStepsNumber = allocationStepsNo;
	NETWORK_STATE.allocationStrategy = allocationName;
	NETWORK_STATE.prioritizationStrategy = prioritizationName;
	NETWORK_STATE.modifications = modificationList;
};

const handleUpdateAllocationData = (msg) => {
	const { allocationSuccess, allocationTime } = msg;

	NETWORK_STATE.avgAllocationSuccessRatio = allocationSuccess;
	NETWORK_STATE.avgAllocationTime = allocationTime;
};

export {
	handleIncrementFinishJobs,
	handleIncrementFinishInCloudJobs,
	handleIncrementFailedJobs,
	handlePlannedJobs,
	handleExecutedJobs,
	handleExecutedInCloudJobs,
	handleCurrentClientsNumber,
	handleUpdateStrategyProperties,
	handleUpdateAllocationAcceptance,
	handleUpdateAllocationData,
};
