import { ClientStatusReportEntry, ReportEntry } from "../../types";
import { Client } from "./types/client-agent";

interface ClientState {
	clients: Client[];
}

interface ClientsReportsState {
	executedJobsReport: ReportEntry[];
	avgCpuReport: ReportEntry[];
	avgMemoryReport: ReportEntry[];
	avgStorageReport: ReportEntry[];
	minCpuReport: ReportEntry[];
	minMemoryReport: ReportEntry[];
	minStorageReport: ReportEntry[];
	maxCpuReport: ReportEntry[];
	maxMemoryReport: ReportEntry[];
	maxStorageReport: ReportEntry[];
	clientsStatusReport: ClientStatusReportEntry[];
	avgClientsExecutionPercentage: ReportEntry[];
	minClientsExecutionPercentage: ReportEntry[];
	maxClientsExecutionPercentage: ReportEntry[];
}

let CLIENTS_STATE: ClientState = {
	clients: [],
};

let CLIENTS_REPORTS_STATE: ClientsReportsState = {
	executedJobsReport: [],
	avgCpuReport: [],
	avgMemoryReport: [],
	avgStorageReport: [],
	minCpuReport: [],
	minMemoryReport: [],
	minStorageReport: [],
	maxCpuReport: [],
	maxMemoryReport: [],
	maxStorageReport: [],
	clientsStatusReport: [],
	avgClientsExecutionPercentage: [],
	minClientsExecutionPercentage: [],
	maxClientsExecutionPercentage: [],
};

const resetClientsState = () =>
	Object.assign(CLIENTS_STATE, {
		clients: [],
	});

const resetClientsReportsState = () =>
	Object.assign(CLIENTS_REPORTS_STATE, {
		executedJobsReport: [],
		avgCpuReport: [],
		avgMemoryReport: [],
		avgStorageReport: [],
		minCpuReport: [],
		minMemoryReport: [],
		minStorageReport: [],
		maxCpuReport: [],
		maxMemoryReport: [],
		maxStorageReport: [],
		clientsStatusReport: [],
		avgClientsExecutionPercentage: [],
		minClientsExecutionPercentage: [],
		maxClientsExecutionPercentage: [],
	});

export {
	CLIENTS_STATE,
	CLIENTS_REPORTS_STATE,
	Client,
	ClientsReportsState,
	resetClientsState,
	resetClientsReportsState,
};
