import { ReportEntry } from "../../types/report-entry-type";

interface NetworkState {
	finishedJobsInCloudNo: number;
	finishedJobsNo: number;
	failedJobsNo: number;
	currPlannedJobsNo: number;
	currActiveJobsNo: number;
	currActiveJobsInCloudNo: number;
	currClientsNo: number;
}

interface NetworkReportsState {
	[key: string]: ReportEntry[];
	failJobsReport: ReportEntry[];
	finishJobsReport: ReportEntry[];
	clientsReport: ReportEntry[];
}

let NETWORK_STATE: NetworkState = {
	finishedJobsInCloudNo: 0,
	finishedJobsNo: 0,
	failedJobsNo: 0,
	currPlannedJobsNo: 0,
	currActiveJobsNo: 0,
	currActiveJobsInCloudNo: 0,
	currClientsNo: 0,
};

let NETWORK_REPORTS_STATE: NetworkReportsState = {
	failJobsReport: [],
	finishJobsReport: [],
	clientsReport: [],
};

const resetNetworkState = () =>
	Object.assign(NETWORK_STATE, {
		finishedJobsInCloudNo: 0,
		finishedJobsNo: 0,
		failedJobsNo: 0,
		currPlannedJobsNo: 0,
		currActiveJobsNo: 0,
		currActiveJobsInCloudNo: 0,
		currClientsNo: 0,
	});

const resetNetworkReportsState = () =>
	Object.assign(NETWORK_REPORTS_STATE, {
		failJobsReport: [],
		finishJobsReport: [],
		clientsReport: [],
	});

export { NETWORK_STATE, NETWORK_REPORTS_STATE, NetworkReportsState, resetNetworkState, resetNetworkReportsState };
