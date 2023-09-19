import { AgentStatisticReport, LiveChartEntry } from 'types/reports'
import { ClientsStatusReport } from 'types/reports/client-reports'

export type ClientReports = {
   executedJobsReport: LiveChartEntry[]
   avgCpuReport: LiveChartEntry[]
   avgMemoryReport: LiveChartEntry[]
   avgStorageReport: LiveChartEntry[]
   minCpuReport: LiveChartEntry[]
   minMemoryReport: LiveChartEntry[]
   minStorageReport: LiveChartEntry[]
   maxCpuReport: LiveChartEntry[]
   maxMemoryReport: LiveChartEntry[]
   maxStorageReport: LiveChartEntry[]
   clientsStatusReport: ClientsStatusReport[]
   avgClientsExecutionPercentage: LiveChartEntry[]
   minClientsExecutionPercentage: LiveChartEntry[]
   maxClientsExecutionPercentage: LiveChartEntry[]
}

export type NetworkReports = {
   clientsReport: LiveChartEntry[]
   finishJobsReport: LiveChartEntry[]
   failJobsReport: LiveChartEntry[]
   executedInServersReport: LiveChartEntry[]
   executedInCloudReport: LiveChartEntry[]
}

export type ManagingSystemReports = {
   jobSuccessRatioReport: LiveChartEntry[]
   trafficDistributionReport: LiveChartEntry[]
   backUpPowerUsageReport: LiveChartEntry[]
}

export type ReportsStore = {
   systemStartTime: number | null
   secondsPerHour: number | null
   systemTrafficReport: LiveChartEntry[]
   agentsReports: AgentStatisticReport[]
} & ClientReports &
   NetworkReports &
   ManagingSystemReports
