import { LiveChartEntry } from '../live-charts/live-chart-entry/live-chart-entry'

export interface AgentCentralManagerStatisticReports {
   clientRequestReport: LiveChartEntry[]
   queueCapacityReport: LiveChartEntry[]
   trafficReport: LiveChartEntry[]
}
