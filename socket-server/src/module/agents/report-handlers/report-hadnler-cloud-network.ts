import { AGENTS_REPORTS_STATE } from "../agents-state";
import { RegionalManagerAgent } from "../types";

const reportRegionalManagerData = (agent: RegionalManagerAgent, time) => {
	const reports = AGENTS_REPORTS_STATE.agentsReports.filter((agentReport) => agentReport.name === agent.name)[0]
		.reports;

	reports["clientsReport"].push({ time, value: agent.totalNumberOfClients });
	reports["trafficReport"].push({ time, value: agent.traffic });
	reports["successRatioReport"].push({ time, value: agent.successRatio ?? 0 });
};

export { reportRegionalManagerData };
