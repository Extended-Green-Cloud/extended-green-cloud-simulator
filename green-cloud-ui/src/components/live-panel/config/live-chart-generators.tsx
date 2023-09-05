/* eslint-disable @typescript-eslint/no-unused-vars */
import {
   AgentCloudNetworkStatisticReports,
   AgentGreenSourceStatisticReports,
   AgentSchedulerStatisticReports,
   AgentServerStatisticReports,
   AgentStatisticReport,
   AgentType,
   CloudNetworkAgent,
   CommonAgentReports,
   JobStatus,
   JobStatusMap,
   LiveChartGenerator,
   ReportsStore,
   ServerAgent
} from '@types'
import {
   AvailableGreenPowerLiveChart,
   ClientJobCPUChart,
   ClientJobExecutionPercentageChart,
   ClientJobStatusTimeChart,
   ClientTypePieChart,
   ClientsNumberLiveChart,
   GreenPowerToBackUpUsageLiveChart,
   GreenToOnHoldLiveChart,
   JobCompletedLiveChart,
   JobExecutionLiveChart,
   QueueCapacityLiveChart,
   SchedulerPriorityLiveChart,
   SuccessRatioChart,
   TrafficDistributionLiveChart,
   TrafficLiveChart
} from '../live-charts'
import { GoalContributionLiveChart, QualityPropertiesLiveChart } from '../live-charts/managing-system-charts'

const getJobCompletionChart: LiveChartGenerator = (reports, _) => {
   const { failJobsReport, finishJobsReport } = reports as ReportsStore
   return <JobCompletedLiveChart {...{ failJobsReport, finishJobsReport }} />
}

const getSystemTrafficChart: LiveChartGenerator = (reports, _) => {
   const { systemTrafficReport: trafficReport } = reports as ReportsStore
   return <TrafficLiveChart {...{ title: 'Total traffic over time', trafficReport }} />
}

const getSystemClientsChart: LiveChartGenerator = (reports, _) => {
   const { clientsReport } = reports as ReportsStore
   return <ClientsNumberLiveChart {...{ title: 'Number of clients over time', clientsReport }} />
}

const getSystemJobExecutionChart: LiveChartGenerator = (reports, _) => {
   const { executedJobsReport, avgCpuReport, minCpuReport, maxCpuReport } = reports as ReportsStore
   return <JobExecutionLiveChart {...{ executedJobsReport, avgCpuReport, minCpuReport, maxCpuReport }} />
}

const getSystemTrafficDistributionChart: LiveChartGenerator = (reports, _) => {
   return <TrafficDistributionLiveChart {...{ reports: reports as ReportsStore }} />
}

const getAgentClientsChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const cnaReports = reportsMapped.reports as AgentCloudNetworkStatisticReports
   const { clientsReport } = cnaReports
   return <ClientsNumberLiveChart {...{ title: `Number of ${reportsMapped.name} clients over time`, clientsReport }} />
}

const getAgentTrafficChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const trafficReport = (reportsMapped.reports as CommonAgentReports).trafficReport
   return <TrafficLiveChart {...{ title: `${reportsMapped.name} traffic over time`, trafficReport }} />
}

const getAgentSuccessRatioChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const successRatio = (reportsMapped.reports as CommonAgentReports).successRatioReport
   return <SuccessRatioChart {...{ title: `${reportsMapped.name} success ratio over time`, successRatio }} />
}

const getAgentBackUpUsageChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { powerConsumptionReport: greenPowerReport, backUpPowerConsumptionReport: backUpPowerReport } =
      reportsMapped.reports as AgentServerStatisticReports
   return (
      <GreenPowerToBackUpUsageLiveChart
         {...{ title: `${reportsMapped.name} green to back-up power usage`, backUpPowerReport, greenPowerReport }}
      />
   )
}

const getAgentJobsOnHoldChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { jobsOnHoldReport, jobsOnGreenPowerReport } = reportsMapped.reports as AgentGreenSourceStatisticReports
   return (
      <GreenToOnHoldLiveChart
         {...{ title: `${reportsMapped.name} jobs on hold over time`, jobsOnHoldReport, jobsOnGreenPowerReport }}
      />
   )
}

const getAgentUsedGreenEnergyChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const availableGreenPower = (reportsMapped.reports as AgentGreenSourceStatisticReports).energyInUseReport
   return (
      <AvailableGreenPowerLiveChart
         {...{ title: `${reportsMapped.name} supplied green energy over time`, availableGreenPower }}
      />
   )
}

const getAgentTrafficDistributionChart: LiveChartGenerator = (reports, _) => (
   <TrafficDistributionLiveChart {...{ reports: reports as ReportsStore }} />
)

const getAgentSchedulerPrioritiesChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { cpuPriorityReport, deadlinePriorityReport } = reportsMapped.reports as AgentSchedulerStatisticReports
   return (
      <SchedulerPriorityLiveChart
         {...{ title: `${reportsMapped.name} job priority over time`, cpuPriorityReport, deadlinePriorityReport }}
      />
   )
}

const getAgentQueueCapacityChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { queueCapacityReport } = reportsMapped.reports as AgentSchedulerStatisticReports
   return (
      <QueueCapacityLiveChart
         {...{ title: `${reportsMapped.name} number of jobs in queue over time`, queueCapacityReport }}
      />
   )
}

const getClientJobCPURequirementChart: LiveChartGenerator = (reports, _) => {
   const reportsMapped = reports as ReportsStore
   const { avgCpuReport, minCpuReport, maxCpuReport } = reportsMapped
   return <ClientJobCPUChart {...{ avgCpuReport, minCpuReport, maxCpuReport }} />
}

const getClientJobExecutionTimeChart: LiveChartGenerator = (reports, _) => {
   const { clientsStatusReport } = reports as ReportsStore
   const initialStatusValues: JobStatusMap = Object.keys(JobStatus).reduce(
      (prev, status) => ({ ...prev, [status]: 0 }),
      {}
   )

   const reportsSize = clientsStatusReport.length
   const aggregatedValues = clientsStatusReport
      .map((report) => report.value)
      .reduce((prev, report) => {
         report.forEach((reportVal) => (prev[reportVal.status] += reportVal.value))
         return prev
      }, initialStatusValues)

   return <ClientJobStatusTimeChart {...{ aggregatedValues, reportLength: reportsSize }} />
}

const getClientJobExecutionPercentageChart: LiveChartGenerator = (reports, _) => {
   const { avgClientsExecutionPercentage, minClientsExecutionPercentage, maxClientsExecutionPercentage } =
      reports as ReportsStore
   return (
      <ClientJobExecutionPercentageChart
         {...{ avgClientsExecutionPercentage, minClientsExecutionPercentage, maxClientsExecutionPercentage }}
      />
   )
}

const getClientProcessorTypeChart: LiveChartGenerator = (reports, _) => {
   return <ClientTypePieChart />
}

const getManagingGoalQualitiesChart: LiveChartGenerator = (reports, _) => {
   const reportsMapped = reports as ReportsStore
   const { jobSuccessRatioReport, trafficDistributionReport, backUpPowerUsageReport } = reportsMapped
   return (
      <QualityPropertiesLiveChart {...{ jobSuccessRatioReport, trafficDistributionReport, backUpPowerUsageReport }} />
   )
}

const getManagingGoalContributionChart: LiveChartGenerator = (reports, _) => {
   return <GoalContributionLiveChart />
}

export {
   getAgentClientsChart,
   getJobCompletionChart,
   getSystemClientsChart,
   getSystemJobExecutionChart,
   getSystemTrafficChart,
   getSystemTrafficDistributionChart,
   getAgentUsedGreenEnergyChart,
   getClientProcessorTypeChart,
   getAgentJobsOnHoldChart,
   getAgentQueueCapacityChart,
   getAgentSchedulerPrioritiesChart,
   getAgentSuccessRatioChart,
   getAgentTrafficChart,
   getAgentTrafficDistributionChart,
   getManagingGoalQualitiesChart,
   getAgentBackUpUsageChart,
   getClientJobCPURequirementChart,
   getManagingGoalContributionChart,
   getClientJobExecutionTimeChart,
   getClientJobExecutionPercentageChart
}
