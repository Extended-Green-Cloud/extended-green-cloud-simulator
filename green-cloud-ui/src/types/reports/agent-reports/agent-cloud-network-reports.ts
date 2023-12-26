import { LiveChartEntry } from '../live-charts/live-chart-entry/live-chart-entry'
import { CommonAgentReports } from './common-agent-reports'

export interface AgentRegionalManagerStatisticReports extends CommonAgentReports {
   clientsReport: LiveChartEntry[]
}
