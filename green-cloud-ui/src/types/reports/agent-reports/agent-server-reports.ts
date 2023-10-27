import { LiveChartEntry } from '../live-charts/live-chart-entry/live-chart-entry'
import { CommonAgentReports } from './common-agent-reports'

export interface AgentServerStatisticReports extends CommonAgentReports {
   cpuInUseReport: LiveChartEntry[]
   powerConsumptionReport: LiveChartEntry[]
   backUpPowerConsumptionReport: LiveChartEntry[]
}
