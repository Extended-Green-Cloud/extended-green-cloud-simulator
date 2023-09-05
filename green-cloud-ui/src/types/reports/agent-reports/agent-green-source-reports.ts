import { CommonAgentReports } from './common-agent-reports'
import { LiveChartEntry } from '../live-charts/live-chart-entry/live-chart-entry'

export interface AgentGreenSourceStatisticReports extends CommonAgentReports {
   availableGreenPowerReport: LiveChartEntry[]
   energyInUseReport: LiveChartEntry[]
   jobsOnGreenPowerReport: LiveChartEntry[]
   jobsOnHoldReport: LiveChartEntry[]
}
