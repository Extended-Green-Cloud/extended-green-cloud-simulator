import { AGENT_TYPES } from "../../constants";
import { ReportEntry } from "../../types";
import { ReportEventEntry } from "../../types/report-event-entry-type";
import { Agent } from "./types";

interface AgentsState {
	agents: Agent[];
}

interface AgentsReports {
	name: string;
	type: AGENT_TYPES;
	reports: AgentReportType;
	events: ReportEventEntry[];
}

interface AgentReportType {
	[key: string]: ReportEntry[];
}

interface AgentsReportsState {
	agentsReports: AgentsReports[];
}

let AGENTS_STATE: AgentsState = {
	agents: [],
};

let AGENTS_REPORTS_STATE: AgentsReportsState = {
	agentsReports: [],
};

const resetAgentsState = () =>
	Object.assign(AGENTS_STATE, {
		agents: [],
	});

const resetAgentsReportsState = () =>
	Object.assign(AGENTS_REPORTS_STATE, {
		agentsReports: [],
	});

export { AGENTS_STATE, AGENTS_REPORTS_STATE, resetAgentsState, resetAgentsReportsState, AgentsReportsState };
