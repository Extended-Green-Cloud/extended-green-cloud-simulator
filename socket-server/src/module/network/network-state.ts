import { ReportEntry } from "../../types/report-entry-type";

interface NetworkState {
	finishedJobsNo: number;
	finishedJobsInCloudNo: number;
	failedJobsNo: number;
	currPlannedJobsNo: number;
	currActiveJobsNo: number;
	currActiveInCloudJobsNo: number;
	currClientsNo: number;
}

interface NetworkReportsState {
	[key: string]: ReportEntry[];
	executedInServersReport: ReportEntry[];
	executedInCloudReport: ReportEntry[];
	failJobsReport: ReportEntry[];
	finishJobsReport: ReportEntry[];
	clientsReport: ReportEntry[];
}

let NETWORK_STATE: NetworkState = {
	finishedJobsNo: 0,
	finishedJobsInCloudNo: 0,
	failedJobsNo: 0,
	currPlannedJobsNo: 0,
	currActiveJobsNo: 0,
	currActiveInCloudJobsNo: 0,
	currClientsNo: 0,
};

let NETWORK_REPORTS_STATE: NetworkReportsState = {
	executedInServersReport: [],
	executedInCloudReport: [],
	failJobsReport: [],
	finishJobsReport: [],
	clientsReport: [],
};

const resetNetworkState = () =>
	Object.assign(NETWORK_STATE, {
		finishedJobsNo: 0,
		finishedJobsInCloudNo: 0,
		failedJobsNo: 0,
		currPlannedJobsNo: 0,
		currActiveJobsNo: 0,
		currActiveInCloudJobsNo: 0,
		currClientsNo: 0,
	});

const resetNetworkReportsState = () =>
	Object.assign(NETWORK_REPORTS_STATE, {
		executedInServersReport: [],
		executedInCloudReport: [],
		failJobsReport: [],
		finishJobsReport: [],
		clientsReport: [],
	});

export { NETWORK_STATE, NETWORK_REPORTS_STATE, NetworkReportsState, resetNetworkState, resetNetworkReportsState };
