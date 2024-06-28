import { ReportEntry } from "../../types/report-entry-type";

interface NetworkState {
	finishedJobsNo: number;
	finishedJobsInCloudNo: number;
	failedJobsNo: number;
	currPlannedJobsNo: number;
	currActiveJobsNo: number;
	currActiveInCloudJobsNo: number;
	currClientsNo: number;
	allocationStrategy: string;
	prioritizationStrategy: string;
	allocationStepsNumber: number;
	modifications: string[];
	avgAllocationTime: number;
	avgAllocationSuccessRatio: number;
	avgAllocationAcceptanceRatio: number;
}

interface NetworkReportsState {
	[key: string]: ReportEntry[];
	executedInServersReport: ReportEntry[];
	executedInCloudReport: ReportEntry[];
	failJobsReport: ReportEntry[];
	finishJobsReport: ReportEntry[];
	clientsReport: ReportEntry[];
	allocationTimeReport: ReportEntry[];
	allocationSuccessRatioReport: ReportEntry[];
	allocationAcceptanceRatioReport: ReportEntry[];
}

let NETWORK_STATE: NetworkState = {
	finishedJobsNo: 0,
	finishedJobsInCloudNo: 0,
	failedJobsNo: 0,
	currPlannedJobsNo: 0,
	currActiveJobsNo: 0,
	currActiveInCloudJobsNo: 0,
	currClientsNo: 0,
	allocationStrategy: "",
	prioritizationStrategy: "",
	allocationStepsNumber: 0,
	modifications: [],
	avgAllocationAcceptanceRatio: 1,
	avgAllocationTime: 0,
	avgAllocationSuccessRatio: 1,
};

let NETWORK_REPORTS_STATE: NetworkReportsState = {
	executedInServersReport: [],
	executedInCloudReport: [],
	failJobsReport: [],
	finishJobsReport: [],
	clientsReport: [],
	allocationTimeReport: [],
	allocationSuccessRatioReport: [],
	allocationAcceptanceRatioReport: [],
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
		allocationStrategy: "",
		prioritizationStrategy: "",
		allocationStepsNumber: 0,
		modifications: [],
		avgAllocationAcceptanceRatio: 1,
		avgAllocationTime: 0,
		avgAllocationSuccessRatio: 1,
	});

const resetNetworkReportsState = () =>
	Object.assign(NETWORK_REPORTS_STATE, {
		executedInServersReport: [],
		executedInCloudReport: [],
		failJobsReport: [],
		finishJobsReport: [],
		clientsReport: [],
		allocationTimeReport: [],
		allocationSuccessRatioReport: [],
		allocationAcceptanceRatioReport: [],
	});

export { NETWORK_STATE, NETWORK_REPORTS_STATE, NetworkReportsState, resetNetworkState, resetNetworkReportsState };
