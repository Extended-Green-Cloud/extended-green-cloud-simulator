import { LiveChartEntry } from '../live-charts/live-chart-entry/live-chart-entry'

export interface AgentSchedulerStatisticReports {
   cpuPriorityReport: LiveChartEntry[]
   deadlinePriorityReport: LiveChartEntry[]
   clientRequestReport: LiveChartEntry[]
   queueCapacityReport: LiveChartEntry[]
   trafficReport: LiveChartEntry[]
}
