import { LiveChartEntry } from '../live-charts/live-chart-entry/live-chart-entry'

export interface CommonAgentReports {
   trafficReport: LiveChartEntry[]
   successRatioReport: LiveChartEntry[]
}
