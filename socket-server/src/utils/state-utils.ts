import {
	AGENTS_REPORTS_STATE,
	AGENTS_STATE,
	AgentsReportsState,
	CLIENTS_REPORTS_STATE,
	CLIENTS_STATE,
	ClientsReportsState,
	GRAPH_STATE,
	MANAGING_SYSTEM_REPORTS,
	MANAGING_SYSTEM_STATE,
	ManagingSystemReportsState,
	NETWORK_REPORTS_STATE,
	NETWORK_STATE,
	NetworkReportsState,
	resetAgentsReportsState,
	resetAgentsState,
	resetClientsReportsState,
	resetClientsState,
	resetGraphState,
	resetManagingSystemReportsState,
	resetManagingSystemState,
	resetNetworkReportsState,
	resetNetworkState,
	resetSimulationState,
	SIMULATION_STATE,
} from "../module";
import { CommonReportEntry } from "../types/common-report-entry";
import { isWithinMonth } from "./time-utils";

const resetSystemState = () => {
	resetAgentsReportsState();
	resetAgentsState();
	resetGraphState();
	resetClientsReportsState();
	resetClientsState();
	resetManagingSystemState();
	resetManagingSystemReportsState();
	resetNetworkReportsState();
	resetNetworkState();
	resetSimulationState();
};

const getSystemState = () => {
	return {
		systemStartTime: SIMULATION_STATE.systemStartTime,
		secondsPerHour: SIMULATION_STATE.systemStartTime,
		network: { ...NETWORK_STATE },
		agents: {
			...CLIENTS_STATE,
			...AGENTS_STATE,
		},
		managingSystem: { ...MANAGING_SYSTEM_STATE },
		graph: { ...GRAPH_STATE },
	};
};

const getAgentsState = () => {
	return {
		...AGENTS_STATE,
		graph: { ...GRAPH_STATE },
	};
};

const getGraphState = () => {
	return {
		...GRAPH_STATE,
	};
};

const getClientsState = () => {
	return {
		clients: CLIENTS_STATE.clients.map((client) => ({
			name: client.name,
			status: client.status,
			processorName: client.job.processorName,
		})),
	};
};

const getClient = (clientName: string) => {
	return CLIENTS_STATE.clients.filter((client) => client.name.toUpperCase() === clientName.toUpperCase())[0] ?? null;
};

const getAgent = (agentName: string) => {
	return AGENTS_STATE.agents.filter((agent) => agent.name.toUpperCase() === agentName.toUpperCase())[0] ?? null;
};

const getManagingState = () => {
	return {
		managing: { ...MANAGING_SYSTEM_STATE },
	};
};

const getNetworkState = () => {
	return {
		network: { ...NETWORK_STATE },
		simulation: { ...SIMULATION_STATE },
	};
};

const filterReports = (reportObject: NetworkReportsState | ClientsReportsState | ManagingSystemReportsState) => {
	return Object.entries(reportObject).reduce(
		(prev, curr) => ({ ...prev, [curr[0]]: curr[1].filter((entry) => isWithinMonth(entry.time)) }),
		{}
	);
};

const filterAgentReports = (agentReports: AgentsReportsState) => {
	return agentReports.agentsReports.length > 0
		? agentReports.agentsReports.map((reportsObject) => ({
				...reportsObject,
				reports: Object.entries(reportsObject.reports).reduce((prev, curr) => {
					return {
						...prev,
						[curr[0]]: curr[1].filter((entry) => isWithinMonth(entry.time)),
					};
				}, {}),
		  }))
		: [];
};

const getNetworkReportsState = () => filterReports(NETWORK_REPORTS_STATE);
const getClientsReportsState = () => filterReports(CLIENTS_REPORTS_STATE);
const getAgentsReportsState = () => filterAgentReports(AGENTS_REPORTS_STATE);
const getManaginReportsState = () => filterReports(MANAGING_SYSTEM_REPORTS);

const getReportsState = () => {
	return {
		...filterReports(NETWORK_REPORTS_STATE),
		...filterReports(CLIENTS_REPORTS_STATE),
		...filterAgentReports(AGENTS_REPORTS_STATE),
	};
};

export {
	resetSystemState,
	getSystemState,
	getAgentsState,
	getGraphState,
	getClientsState,
	getClient,
	getAgent,
	getManagingState,
	getNetworkState,
	getReportsState,
	getNetworkReportsState,
	getClientsReportsState,
	getAgentsReportsState,
	getManaginReportsState,
};
