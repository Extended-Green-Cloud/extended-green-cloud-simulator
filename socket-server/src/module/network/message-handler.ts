import { NETWORK_STATE } from "./network-state";

const handleIncrementFinishJobs = (msg) => (NETWORK_STATE.finishedJobsNo += msg.data);
const handleIncrementFinishInCloudJobs = (msg) => (NETWORK_STATE.finishedJobsInCloudNo += msg.data);
const handleIncrementFailedJobs = (msg) => (NETWORK_STATE.failedJobsNo += msg.data);
const handlePlannedJobs = (msg) => (NETWORK_STATE.currPlannedJobsNo += msg.data);
const handleExecutedJobs = (msg) => (NETWORK_STATE.currActiveJobsNo += msg.data);
const handleExecutedInCloudJobs = (msg) => (NETWORK_STATE.currActiveInCloudJobsNo += msg.data);
const handleCurrentClientsNumber = (msg) => (NETWORK_STATE.currClientsNo += msg.data);

export {
	handleIncrementFinishJobs,
	handleIncrementFinishInCloudJobs,
	handleIncrementFailedJobs,
	handlePlannedJobs,
	handleExecutedJobs,
	handleExecutedInCloudJobs,
	handleCurrentClientsNumber,
};
