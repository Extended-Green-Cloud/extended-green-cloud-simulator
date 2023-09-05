import {
   AgentServerStatisticReports,
   AgentStatisticReport,
   LiveIndicatorAvgGenerator,
   ReportsStore,
   ServerAgent
} from '@types'
import { getAverage } from '@utils'
import { useSelector } from 'react-redux'
import { RootState, selectChosenNetworkAgent } from '@store'
import { getJobResourceVal, getJobStatusDuration } from 'utils/job-utils'

const getSystemAvgTrafficIndicator: LiveIndicatorAvgGenerator = (reports) =>
   Math.round(getAverage((reports as ReportsStore).systemTrafficReport, 'value'))

const getSystemAvgClientsIndicator: LiveIndicatorAvgGenerator = (reports) =>
   Math.round(getAverage((reports as ReportsStore).clientsReport, 'value'))

const getAvgInUseCpuIndicator: LiveIndicatorAvgGenerator = (reports) => {
   const selectedAgent = useSelector((state: RootState) => selectChosenNetworkAgent(state)) as ServerAgent
   const reportsMapped = reports as AgentStatisticReport
   const { cpuInUseReport } = reportsMapped.reports as AgentServerStatisticReports
   return (getAverage(cpuInUseReport, 'value') / selectedAgent.cpu).toFixed(1)
}

const getAvgMemoryIndicator: LiveIndicatorAvgGenerator = (reports) => {
   const selectedAgent = useSelector((state: RootState) => selectChosenNetworkAgent(state)) as ServerAgent
   const reportsMapped = reports as AgentStatisticReport
   const { memoryInUseReport } = reportsMapped.reports as AgentServerStatisticReports
   return (getAverage(memoryInUseReport, 'value') / selectedAgent.memory).toFixed(1)
}

const getAvgStorageIndicator: LiveIndicatorAvgGenerator = (reports) => {
   const selectedAgent = useSelector((state: RootState) => selectChosenNetworkAgent(state)) as ServerAgent
   const reportsMapped = reports as AgentStatisticReport
   const { storageInUseReport } = reportsMapped.reports as AgentServerStatisticReports
   return (getAverage(storageInUseReport, 'value') / selectedAgent.storage).toFixed(1)
}

const getSystemAvgJobsIndicator: LiveIndicatorAvgGenerator = (reports) =>
   Math.round(getAverage((reports as ReportsStore).executedJobsReport, 'value'))

const getAvgInProgressJobTime: LiveIndicatorAvgGenerator = (reports) => {
   const { clientsStatusReport } = reports as ReportsStore
   const inProgressValues = clientsStatusReport
      .map((entry) => entry.value)
      .flatMap((entry) => entry)
      .filter((entry) => entry.status === 'IN_PROGRESS')
   const avgInProgress = Math.round(getAverage(inProgressValues, 'value'))

   return getJobStatusDuration('IN_PROGRESS', avgInProgress)
}

const getAverageJobExecutionPercentage: LiveIndicatorAvgGenerator = (reports) =>
   Math.round((reports as ReportsStore).avgClientsExecutionPercentage.slice(-1)[0]?.value ?? 0)

const getAverageCpu: LiveIndicatorAvgGenerator = (reports) =>
   getJobResourceVal(Math.round(getAverage((reports as ReportsStore).avgCpuReport, 'value')))

const getAverageMemory: LiveIndicatorAvgGenerator = (reports) =>
   getJobResourceVal(Math.round(getAverage((reports as ReportsStore).avgMemoryReport, 'value')))

const getAverageStorage: LiveIndicatorAvgGenerator = (reports) =>
   getJobResourceVal(Math.round(getAverage((reports as ReportsStore).avgStorageReport, 'value')))

export {
   getSystemAvgClientsIndicator,
   getSystemAvgJobsIndicator,
   getSystemAvgTrafficIndicator,
   getAverageJobExecutionPercentage,
   getAvgInProgressJobTime,
   getAvgInUseCpuIndicator,
   getAvgMemoryIndicator,
   getAvgStorageIndicator,
   getAverageCpu,
   getAverageMemory,
   getAverageStorage
}
