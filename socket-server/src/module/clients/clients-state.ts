import { ClientStatusReportEntry, ReportEntry } from "../../types";
import { Client } from "./types/client-agent";

interface ClientState {
	clients: Client[];
}

interface ClientsReportsState {
	executedJobsReport: ReportEntry[];
	avgCpuReport: ReportEntry[];
	minCpuReport: ReportEntry[];
	maxCpuReport: ReportEntry[];
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
	minCpuReport: [],
	maxCpuReport: [],
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
		minCpuReport: [],
		maxCpuReport: [],
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
