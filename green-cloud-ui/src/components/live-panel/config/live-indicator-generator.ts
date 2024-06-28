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
   return (
      getAverage(cpuInUseReport, 'value') / selectedAgent.resources?.['cpu'].characteristics['amount']?.value ?? 0
   ).toFixed(1)
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

const getAverageBackUpPowerConsumption: LiveIndicatorAvgGenerator = (reports) =>
   getJobResourceVal(
      Math.round(
         getAverage(
            ((reports as AgentStatisticReport).reports as AgentServerStatisticReports).backUpPowerConsumptionReport,
            'value'
         )
      )
   )

const getAverageAllocationSuccess: LiveIndicatorAvgGenerator = (reports) =>
   (reports as ReportsStore).allocationSuccessRatioReport[-1].value

const getAverageAllocationAcceptance: LiveIndicatorAvgGenerator = (reports) =>
   (reports as ReportsStore).allocationAcceptanceRatioReport[-1].value

export {
   getSystemAvgClientsIndicator,
   getSystemAvgJobsIndicator,
   getSystemAvgTrafficIndicator,
   getAverageJobExecutionPercentage,
   getAvgInProgressJobTime,
   getAvgInUseCpuIndicator,
   getAverageCpu,
   getAverageBackUpPowerConsumption,
   getAverageAllocationAcceptance,
   getAverageAllocationSuccess
}
