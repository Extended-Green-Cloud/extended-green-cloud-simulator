import {
   AgentType,
   LiveIndicatorConfiguration,
   LiveIndicatorAvgGeneratorType,
   LiveChartDashboard,
   TimeOptions
} from '@types'
import { PercentageIndicator, ValueIndicator } from 'components/common'
import { IconChip, IconClients, IconGear } from '@assets'
import {
   getAgentUsedGreenEnergyChart,
   getAgentClientsChart,
   getAgentJobsOnHoldChart,
   getAgentQueueCapacityChart,
   getAgentSuccessRatioChart,
   getAgentTrafficChart,
   getAgentTrafficDistributionChart,
   getClientJobExecutionPercentageChart,
   getClientJobExecutionTimeChart,
   getJobCompletionChart,
   getManagingGoalContributionChart,
   getManagingGoalQualitiesChart,
   getSystemClientsChart,
   getSystemJobExecutionChart,
   getSystemTrafficChart,
   getSystemTrafficDistributionChart,
   getAgentBackUpUsageChart,
   getClientProcessorTypeChart,
   getClientJobCPURequirementChart,
   getSystemJobTypeChart
} from './live-chart-generators'
import {
   getAverageBackUpPowerConsumption,
   getAverageCpu,
   getAverageJobExecutionPercentage,
   getAvgInProgressJobTime,
   getAvgInUseCpuIndicator,
   getSystemAvgClientsIndicator,
   getSystemAvgJobsIndicator,
   getSystemAvgTrafficIndicator
} from './live-indicator-generator'

export const CHARTS = {
   systemJobFinishSuccess: getJobCompletionChart,
   systemTraffic: getSystemTrafficChart,
   systemClients: getSystemClientsChart,
   systemJobExecution: getSystemJobExecutionChart,
   systemTrafficDistribution: getSystemTrafficDistributionChart,
   systemJobExecutionTypeChart: getSystemJobTypeChart,
   agentClients: getAgentClientsChart,
   agentTraffic: getAgentTrafficChart,
   agentSuccessRatio: getAgentSuccessRatioChart,
   agentBackUpUsage: getAgentBackUpUsageChart,
   agentJobsOnHold: getAgentJobsOnHoldChart,
   agentAvailableGreenPower: getAgentUsedGreenEnergyChart,
   agentTrafficDistribution: getAgentTrafficDistributionChart,
   agentQueueCapacity: getAgentQueueCapacityChart,
   clientJobCpuChart: getClientJobCPURequirementChart,
   clientJobStatusTime: getClientJobExecutionTimeChart,
   clientProcessorTypes: getClientProcessorTypeChart,
   clientJobExecutionPercentage: getClientJobExecutionPercentageChart,
   managingGoalQualities: getManagingGoalQualitiesChart,
   managingGoalContribution: getManagingGoalContributionChart
}

interface AvgOptions {
   systemAvgTraffic: LiveIndicatorConfiguration
   systemAvgClients: LiveIndicatorConfiguration
   systemAvgJobs: LiveIndicatorConfiguration
   jobExecutionAvgTime: LiveIndicatorConfiguration
   jobExecutionAvgPercentage: LiveIndicatorConfiguration
   clientsAvgCpu: LiveIndicatorConfiguration
   serverInUseCpu: LiveIndicatorConfiguration
   serverAvgBackUpPowerConsumption: LiveIndicatorConfiguration
}

const AVG_INDICATORS: AvgOptions = {
   systemAvgTraffic: {
      title: 'Average traffic',
      type: LiveIndicatorAvgGeneratorType.REPORT,
      value: getSystemAvgTrafficIndicator,
      indicator: PercentageIndicator
   },
   systemAvgClients: {
      title: 'Average client number',
      type: LiveIndicatorAvgGeneratorType.REPORT,
      value: getSystemAvgClientsIndicator,
      icon: IconClients,
      indicator: ValueIndicator
   },
   systemAvgJobs: {
      title: 'Average jobs number',
      type: LiveIndicatorAvgGeneratorType.REPORT,
      value: getSystemAvgJobsIndicator,
      icon: IconGear,
      indicator: ValueIndicator
   },
   jobExecutionAvgTime: {
      title: 'Average job execution time',
      type: LiveIndicatorAvgGeneratorType.REPORT,
      value: getAvgInProgressJobTime,
      indicator: ValueIndicator
   },
   jobExecutionAvgPercentage: {
      title: 'Average job execution percentage',
      type: LiveIndicatorAvgGeneratorType.REPORT,
      value: getAverageJobExecutionPercentage,
      indicator: PercentageIndicator
   },
   serverInUseCpu: {
      title: 'Average CPU utilization',
      type: LiveIndicatorAvgGeneratorType.AGENT_REPORT,
      value: getAvgInUseCpuIndicator,
      indicator: PercentageIndicator
   },
   serverAvgBackUpPowerConsumption: {
      title: 'Average back-up utilization',
      type: LiveIndicatorAvgGeneratorType.AGENT_REPORT,
      value: getAverageBackUpPowerConsumption,
      indicator: PercentageIndicator
   },
   clientsAvgCpu: {
      title: 'Average CPU',
      type: LiveIndicatorAvgGeneratorType.REPORT,
      value: getAverageCpu,
      icon: IconChip,
      indicator: ValueIndicator
   }
}

export const CHART_MODALS: LiveChartDashboard = {
   cloud: {
      name: 'System statistics reports',
      charts: [
         CHARTS.systemClients,
         CHARTS.systemJobExecutionTypeChart,
         CHARTS.systemJobExecution,
         CHARTS.systemJobFinishSuccess,
         CHARTS.systemTraffic,
         CHARTS.systemTrafficDistribution
      ],
      mainChartId: 3,
      valueFields: [AVG_INDICATORS.systemAvgTraffic, AVG_INDICATORS.systemAvgClients, AVG_INDICATORS.systemAvgJobs]
   },
   clients: {
      name: 'Clients statistics reports',
      charts: [
         CHARTS.clientJobExecutionPercentage,
         CHARTS.clientJobStatusTime,
         CHARTS.clientJobCpuChart,
         CHARTS.clientProcessorTypes
      ],
      mainChartId: 0,
      disableChartDashboard: false,
      valueFields: [
         AVG_INDICATORS.jobExecutionAvgPercentage,
         AVG_INDICATORS.jobExecutionAvgTime,
         AVG_INDICATORS.clientsAvgCpu
      ]
   },
   adaptation: {
      name: 'Managing system reports',
      charts: [CHARTS.managingGoalQualities, CHARTS.managingGoalContribution],
      mainChartId: 0,
      disableChartDashboard: false,
      valueFields: []
   },
   [`agent${AgentType.CENTRAL_MANAGER}`]: {
      name: 'Central Manager Agent reports',
      charts: [CHARTS.agentQueueCapacity],
      disableChartDashboard: true,
      mainChartId: 0,
      valueFields: []
   },
   [`agent${AgentType.REGIONAL_MANAGER}`]: {
      name: 'Regional Manager Agent reports',
      charts: [CHARTS.agentClients, CHARTS.agentSuccessRatio, CHARTS.agentTraffic, CHARTS.agentTrafficDistribution],
      mainChartId: 2,
      valueFields: []
   },
   [`agent${AgentType.SERVER}`]: {
      name: 'Server Agent reports',
      charts: [CHARTS.agentSuccessRatio, CHARTS.agentTraffic, CHARTS.agentBackUpUsage, CHARTS.agentTrafficDistribution],
      mainChartId: 1,
      valueFields: [AVG_INDICATORS.serverInUseCpu, AVG_INDICATORS.serverAvgBackUpPowerConsumption]
   },
   [`agent${AgentType.GREEN_ENERGY}`]: {
      name: 'Green Energy Agent reports',
      charts: [CHARTS.agentSuccessRatio, CHARTS.agentTraffic, CHARTS.agentJobsOnHold, CHARTS.agentAvailableGreenPower],
      mainChartId: 3,
      valueFields: []
   }
}

export const TIME_OPTIONS: TimeOptions = {
   DAY: { label: 'DAY', value: 60 * 24, isSelected: true },
   WEEK: { label: 'WEEK', value: 60 * 24 * 7, isSelected: false },
   MONTH: { label: '30 DAYS', value: 60 * 24 * 7 * 30, isSelected: false }
}

export const GOALS_COLORS = {
   SUCCESS_RATIO: 'var(--green-1)',
   TRAFFIC_DISTRIBUTION: 'var(--green-4)',
   BACK_UP_POWER: 'var(--green-6)'
}

export const getColorByName = (name: string) => {
   if (name.toUpperCase() === 'MAXIMIZE JOB SUCCESS RATIO') return GOALS_COLORS.SUCCESS_RATIO
   if (name.toUpperCase() === 'DISTRIBUTE TRAFFIC EVENLY') return GOALS_COLORS.TRAFFIC_DISTRIBUTION
   return GOALS_COLORS.BACK_UP_POWER
}

export const PIE_COLORS = [
   'var(--green-1)',
   'var(--green-4)',
   'var(--green-6)',
   'var(--green-2)',
   'var(--green-3)',
   'var(--green-5)',
   'var(--olive-1)',
   'var(--olive-2)',
   'var(--olive-3)',
   'var(--olive-4)'
]
