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
   AgentMaximumCapacityLiveChart,
   AvailableGreenPowerLiveChart,
   ClientJobExecutionLiveChart,
   ClientJobExecutionPercentageChart,
   ClientJobProportionExecutionChart,
   ClientJobStatusTimeChart,
   ClientsNumberLiveChart,
   GreenPowerToBackUpUsageLiveChart,
   GreenToOnHoldLiveChart,
   JobCompletedLiveChart,
   JobExecutionLiveChart,
   JobExecutionTypeChart,
   QueueCapacityLiveChart,
   SchedulerPriorityLiveChart,
   SuccessRatioChart,
   TrafficDistributionLiveChart,
   TrafficLiveChart
} from '../live-charts'
import { useSelector } from 'react-redux'
import { RootState, selectAdaptationGoals, selectChosenNetworkAgent, selectNetworkStatistics } from '@store'
import { GoalContributionLiveChart, QualityPropertiesLiveChart } from '../live-charts/managing-system-charts'

const getJobCompletationChart: LiveChartGenerator = (reports, _) => {
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

const getJobExecutionTypeChart: LiveChartGenerator = (reports, _) => {
   const { finishedJobsInCloudNo, finishedJobsNo } = useSelector((state: RootState) => selectNetworkStatistics(state))
   const jobsExecutedInCloud = (finishedJobsNo !== 0 ? finishedJobsInCloudNo / finishedJobsNo : 0) * 100
   const jobsExecutedWithGreen = finishedJobsNo !== 0 ? 100 - jobsExecutedInCloud : 0
   return <JobExecutionTypeChart {...{ jobsExecutedInCloud, jobsExecutedWithGreen }} />
}

const getSystemJobExecutionChart: LiveChartGenerator = (reports, _) => {
   const { executedJobsReport, avgJobSizeReport, minJobSizeReport, maxJobSizeReport } = reports as ReportsStore
   return <JobExecutionLiveChart {...{ executedJobsReport, avgJobSizeReport, minJobSizeReport, maxJobSizeReport }} />
}

const getSystemTrafficDistributionChart: LiveChartGenerator = (reports, _) => {
   const agentReports = (reports as ReportsStore).agentsReports.filter(
      (report) => report.type === AgentType.CLOUD_NETWORK
   )
   return <TrafficDistributionLiveChart {...{ title: 'Traffic distribution per CNA', agentReports }} />
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

const getagentBackUpUsageChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { greenPowerUsageReport: greenPowerReport, backUpPowerUsageReport: backUpPowerReport } =
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

const getAgentAvailableGreenPowerChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const availableGreenPower = (reportsMapped.reports as AgentGreenSourceStatisticReports).availableGreenPowerReport
   return (
      <AvailableGreenPowerLiveChart
         {...{ title: `${reportsMapped.name} available green power over time`, availableGreenPower }}
      />
   )
}

const getAgentTrafficDistributionChart: LiveChartGenerator = (reports) => {
   const selectedAgent = useSelector((state: RootState) => selectChosenNetworkAgent(state))
   const connectedAgents =
      selectedAgent?.type === AgentType.CLOUD_NETWORK
         ? { type: 'Servers', agents: (selectedAgent as CloudNetworkAgent).serverAgents }
         : { type: 'Green Sources', agents: (selectedAgent as ServerAgent).greenEnergyAgents }

   const reportsStore = reports as ReportsStore
   const agentReports = reportsStore.agentsReports.filter((report) => connectedAgents.agents.includes(report.name))
   return (
      <TrafficDistributionLiveChart
         {...{ title: `${selectedAgent?.name} traffic distribution per ${connectedAgents.type}`, agentReports }}
      />
   )
}

const getAgentMaximumCapacityChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { capacityReport } = reportsMapped.reports as CommonAgentReports
   return (
      <AgentMaximumCapacityLiveChart
         {...{ title: `Maximum capacity of ${reportsMapped.name} over time`, capacityReport }}
      />
   )
}

const getAgentSchedulerPrioritiesChart: LiveChartGenerator = (_, agentReports) => {
   const reportsMapped = agentReports as AgentStatisticReport
   const { powerPriorityReport, deadlinePriorityReport } = reportsMapped.reports as AgentSchedulerStatisticReports
   return (
      <SchedulerPriorityLiveChart
         {...{ title: `${reportsMapped.name} job priority over time`, powerPriorityReport, deadlinePriorityReport }}
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

const getClientJobExecutionSizeChart: LiveChartGenerator = (reports, _) => {
   const reportsMapped = reports as ReportsStore
   const { avgJobSizeReport, maxJobSizeReport, minJobSizeReport } = reportsMapped
   return <ClientJobExecutionLiveChart {...{ avgJobSizeReport, minJobSizeReport, maxJobSizeReport }} />
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

const getClientJobProportionChart: LiveChartGenerator = (reports, _) => {
   const { jobsExecutedAsWhole, jobsExecutedInParts } = reports as ReportsStore
   return <ClientJobProportionExecutionChart {...{ jobsExecutedAsWhole, jobsExecutedInParts }} />
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

const getManagingGoalQualitiesChart: LiveChartGenerator = (reports, _) => {
   const reportsMapped = reports as ReportsStore
   const { jobSuccessRatioReport, trafficDistributionReport, backUpPowerUsageReport } = reportsMapped
   return (
      <QualityPropertiesLiveChart {...{ jobSuccessRatioReport, trafficDistributionReport, backUpPowerUsageReport }} />
   )
}

const getManagingGoalContributionChart: LiveChartGenerator = (reports, _) => {
   const goals = useSelector((state: RootState) => selectAdaptationGoals(state))
   return <GoalContributionLiveChart {...{ goals }} />
}

export {
   getAgentClientsChart,
   getJobCompletationChart,
   getSystemClientsChart,
   getSystemJobExecutionChart,
   getSystemTrafficChart,
   getSystemTrafficDistributionChart,
   getAgentAvailableGreenPowerChart,
   getAgentJobsOnHoldChart,
   getAgentMaximumCapacityChart,
   getAgentQueueCapacityChart,
   getAgentSchedulerPrioritiesChart,
   getAgentSuccessRatioChart,
   getAgentTrafficChart,
   getAgentTrafficDistributionChart,
   getClientJobExecutionSizeChart,
   getManagingGoalQualitiesChart,
   getagentBackUpUsageChart,
   getManagingGoalContributionChart,
   getJobExecutionTypeChart,
   getClientJobExecutionTimeChart,
   getClientJobProportionChart,
   getClientJobExecutionPercentageChart
}
