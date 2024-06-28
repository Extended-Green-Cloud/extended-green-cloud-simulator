import { LiveIndicatorConfiguration } from 'types/reports/live-indicator'
import { LiveChartGenerator } from '../live-chart-generators'
import { LiveChartElementType } from './live-chart-element-type'

export interface LiveChartDashboardType {
   name: string
   charts: LiveChartGenerator[]
   displayedElementId: number
   displayedElementType: LiveChartElementType
   disableChartDashboard?: boolean
   valueFields?: LiveIndicatorConfiguration[]
}
