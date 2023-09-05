import { JOB_STATUSES } from "../../constants/constants";
import { CLIENTS_REPORTS_STATE, CLIENTS_STATE } from "./clients-state";

const reportExecutedJob = (time: number) => {
	const activeStatuses = [JOB_STATUSES.IN_PROGRESS, JOB_STATUSES.ON_BACK_UP, JOB_STATUSES.ON_HOLD];
	const jobsNo = CLIENTS_STATE.clients.filter((client) => activeStatuses.includes(client.status)).length;

	return { time, value: jobsNo };
};

const reportJobSizeData = (time: number) => {
	const activeStatuses = [JOB_STATUSES.IN_PROGRESS, JOB_STATUSES.ON_BACK_UP, JOB_STATUSES.ON_HOLD];
	const jobsCpu = CLIENTS_STATE.clients
		.filter((client) => activeStatuses.includes(client.status))
		.map((client) => client.job.cpu);
	const jobsMemory = CLIENTS_STATE.clients
		.filter((client) => activeStatuses.includes(client.status))
		.map((client) => client.job.memory);
	const jobsStorage = CLIENTS_STATE.clients
		.filter((client) => activeStatuses.includes(client.status))
		.map((client) => client.job.storage);

	const isEmpty = jobsCpu.length === 0;
	const avgCpu = !isEmpty ? jobsCpu.reduce((size1, size2) => size1 + size2, 0) / jobsCpu.length : 0;
	const avgMemory = !isEmpty ? jobsMemory.reduce((size1, size2) => size1 + size2, 0) / jobsMemory.length : 0;
	const avgStorage = !isEmpty ? jobsStorage.reduce((size1, size2) => size1 + size2, 0) / jobsStorage.length : 0;

	return {
		time,
		avgCpu,
		avgMemory,
		avgStorage,
		minCpu: isEmpty ? 0 : Math.min(...jobsCpu),
		minMemory: isEmpty ? 0 : Math.min(...jobsMemory),
		minStorage: isEmpty ? 0 : Math.min(...jobsStorage),
		maxCpu: isEmpty ? 0 : Math.max(...jobsCpu),
		maxMemory: isEmpty ? 0 : Math.max(...jobsMemory),
		maxStorage: isEmpty ? 0 : Math.max(...jobsStorage),
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
	const avgMemoryReport = CLIENTS_REPORTS_STATE.avgMemoryReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.avgMemory,
	});
	const avgStorageReport = CLIENTS_REPORTS_STATE.avgStorageReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.avgStorage,
	});
	const minCpuReport = CLIENTS_REPORTS_STATE.minCpuReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.minCpu,
	});
	const minMemoryReport = CLIENTS_REPORTS_STATE.minMemoryReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.minMemory,
	});
	const minStorageReport = CLIENTS_REPORTS_STATE.minStorageReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.minStorage,
	});
	const maxCpuReport = CLIENTS_REPORTS_STATE.maxCpuReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.maxCpu,
	});
	const maxMemoryReport = CLIENTS_REPORTS_STATE.maxMemoryReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.maxMemory,
	});
	const maxStorageReport = CLIENTS_REPORTS_STATE.maxStorageReport.concat({
		time: jobSizeData.time,
		value: jobSizeData.maxStorage,
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
		avgMemoryReport,
		avgStorageReport,
		minCpuReport,
		minMemoryReport,
		minStorageReport,
		maxCpuReport,
		maxMemoryReport,
		maxStorageReport,
		clientsStatusReport,
		avgClientsExecutionPercentage,
		minClientsExecutionPercentage,
		maxClientsExecutionPercentage,
	});
};

export { updateClientReportsState };
