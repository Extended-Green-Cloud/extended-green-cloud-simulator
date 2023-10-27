import { JOB_STATUSES } from "../../constants/constants";
import { CLIENTS_REPORTS_STATE, CLIENTS_STATE } from "./clients-state";

const reportExecutedJob = (time: number) => {
	const activeStatuses = [
		JOB_STATUSES.IN_PROGRESS,
		JOB_STATUSES.IN_PROGRESS_CLOUD,
		JOB_STATUSES.ON_BACK_UP,
		JOB_STATUSES.ON_HOLD,
	];
	const jobsNo = CLIENTS_STATE.clients.filter((client) => activeStatuses.includes(client.status)).length;

	return { time, value: jobsNo };
};

const reportJobSizeData = (time: number) => {
	const activeStatuses = [
		JOB_STATUSES.IN_PROGRESS,
		JOB_STATUSES.IN_PROGRESS_CLOUD,
		JOB_STATUSES.ON_BACK_UP,
		JOB_STATUSES.ON_HOLD,
	];
	const jobsCpu = CLIENTS_STATE.clients
		.filter((client) => activeStatuses.includes(client.status))
		.map((client) => client.job.resources["cpu"]?.characteristics?.["amount"].value ?? 0);

	const isEmpty = jobsCpu.length === 0;
	const avgCpu = !isEmpty ? jobsCpu.reduce((size1, size2) => size1 + size2, 0) / jobsCpu.length : 0;

	return {
		time,
		avgCpu,
		minCpu: isEmpty ? 0 : Math.min(...jobsCpu),
		maxCpu: isEmpty ? 0 : Math.max(...jobsCpu),
	};
};

const reportJobStatusExecutionTime = () => {
	const clientsNo = CLIENTS_STATE.clients.length;
	const clientsDurationMaps = CLIENTS_STATE.clients.map((client) => client.durationMap);

	return Object.keys(JOB_STATUSES).map((status) => {
		const value =
			clientsDurationMaps
				?.map((durationMap) => (durationMap === null ? 0 : durationMap[status]))
				.reduce((prev, curr) => prev + curr, 0) ?? 0;
		return { status, value: clientsNo !== 0 ? value / clientsNo : 0 };
	});
};

const reportJobExecutionPercentages = () => {
	const clientsNo = CLIENTS_STATE.clients.length;
	const clientsPercentages = CLIENTS_STATE.clients
		.filter((client) => client.status === JOB_STATUSES.FINISHED)
		.map((client) => client.jobExecutionProportion);

	const clientPercentageSum = clientsPercentages.reduce((prev, curr) => prev + curr, 0);
	const avgPercentage = (clientsNo !== 0 ? clientPercentageSum / clientsNo : 0) * 100;
	const minPercentage = Math.min(...clientsPercentages) * 100;
	const maxPercentage = Math.max(...clientsPercentages) * 100;

	return { avgPercentage, minPercentage, maxPercentage };
};

const updateClientReportsState = (time) => {
	const jobSizeData = reportJobSizeData(time);
	const jobPercentages = reportJobExecutionPercentages();

	const executedJobsReport = CLIENTS_REPORTS_STATE.executedJobsReport.concat(reportExecutedJob(time));
	const avgCpuReport = CLIENTS_REPORTS_STATE.avgCpuReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.avgCpu,
	});
	const minCpuReport = CLIENTS_REPORTS_STATE.minCpuReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.minCpu,
	});
	const maxCpuReport = CLIENTS_REPORTS_STATE.maxCpuReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.maxCpu,
	});
	const clientsStatusReport = CLIENTS_REPORTS_STATE.clientsStatusReport.concat({
		time,
		value: reportJobStatusExecutionTime(),
	});
	const avgClientsExecutionPercentage = CLIENTS_REPORTS_STATE.avgClientsExecutionPercentage.concat({
		time,
		value: jobPercentages.avgPercentage,
	});
	const minClientsExecutionPercentage = CLIENTS_REPORTS_STATE.minClientsExecutionPercentage.concat({
		time,
		value: jobPercentages.minPercentage,
	});
	const maxClientsExecutionPercentage = CLIENTS_REPORTS_STATE.maxClientsExecutionPercentage.concat({
		time,
		value: jobPercentages.maxPercentage,
	});

	Object.assign(CLIENTS_REPORTS_STATE, {
		executedJobsReport,
		avgCpuReport,
		minCpuReport,
		maxCpuReport,
		clientsStatusReport,
		avgClientsExecutionPercentage,
		minClientsExecutionPercentage,
		maxClientsExecutionPercentage,
	});
};

export { updateClientReportsState };
