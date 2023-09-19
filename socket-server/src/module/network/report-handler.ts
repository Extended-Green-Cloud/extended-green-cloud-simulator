import { NETWORK_REPORTS_STATE, NETWORK_STATE } from "./network-state";

const reportFailedJob = (time: number) => ({ time, value: NETWORK_STATE.failedJobsNo });
const reportFinishedJob = (time: number) => ({ time, value: NETWORK_STATE.finishedJobsNo });
const reportClients = (time: number) => ({ time, value: NETWORK_STATE.currClientsNo });
const reportJobsInServers = (time: number) => ({ time, value: NETWORK_STATE.currActiveJobsNo });
const reportJobsInCloud = (time: number) => ({ time, value: NETWORK_STATE.currActiveInCloudJobsNo });

const updateNetworkReportsState = (time) => {
	const failJobsReport = NETWORK_REPORTS_STATE.failJobsReport.concat(reportFailedJob(time));
	const finishJobsReport = NETWORK_REPORTS_STATE.finishJobsReport.concat(reportFinishedJob(time));
	const clientsReport = NETWORK_REPORTS_STATE.clientsReport.concat(reportClients(time));
	const executedInCloudReport = NETWORK_REPORTS_STATE.executedInCloudReport.concat(reportJobsInCloud(time));
	const executedInServersReport = NETWORK_REPORTS_STATE.executedInServersReport.concat(reportJobsInServers(time));

	Object.assign(NETWORK_REPORTS_STATE, {
		failJobsReport,
		finishJobsReport,
		clientsReport,
		executedInCloudReport,
		executedInServersReport,
	});
};

export { updateNetworkReportsState };
