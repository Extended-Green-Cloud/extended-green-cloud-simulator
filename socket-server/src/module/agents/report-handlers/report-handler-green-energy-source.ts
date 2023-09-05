import { AGENTS_REPORTS_STATE } from "../agents-state";
import { GreenEnergyAgent } from "../types";

const reportGreenSourceData = (agent: GreenEnergyAgent, time) => {
	const reports = AGENTS_REPORTS_STATE.agentsReports.filter((agentReport) => agentReport.name === agent.name)[0]
		.reports;

	reports["trafficReport"].push({ time, value: agent.traffic });
	reports["availableGreenPowerReport"].push({ time, value: agent.availableGreenEnergy });
	reports["energyInUseReport"].push({ time, value: agent.energyInUse });
	reports["jobsOnGreenPowerReport"].push({ time, value: agent.numberOfExecutedJobs });
	reports["jobsOnHoldReport"].push({ time, value: agent.numberOfJobsOnHold });
	reports["successRatioReport"].push({ time, value: agent.successRatio ?? 0 });
};

export { reportGreenSourceData };
