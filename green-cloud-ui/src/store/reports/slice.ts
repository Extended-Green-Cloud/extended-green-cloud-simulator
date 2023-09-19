import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import {
   AgentSchedulerStatisticReports,
   AgentStatisticReport,
   AgentType,
   FetchClientReportsMessage,
   FetchManagingReportsMessage,
   FetchNetworkReportsMessage,
   ReportsStore,
   SystemTimeMessage
} from '@types'

const INITIAL_STATE: ReportsStore = {
   systemStartTime: null,
   secondsPerHour: null,
   finishJobsReport: [],
   failJobsReport: [],
   executedJobsReport: [],
   executedInServersReport: [],
   executedInCloudReport: [],
   systemTrafficReport: [],
   clientsReport: [],
   avgCpuReport: [],
   avgMemoryReport: [],
   avgStorageReport: [],
   minCpuReport: [],
   minMemoryReport: [],
   minStorageReport: [],
   maxCpuReport: [],
   maxMemoryReport: [],
   maxStorageReport: [],
   agentsReports: [],
   jobSuccessRatioReport: [],
   trafficDistributionReport: [],
   backUpPowerUsageReport: [],
   clientsStatusReport: [],
   avgClientsExecutionPercentage: [],
   minClientsExecutionPercentage: [],
   maxClientsExecutionPercentage: []
}

/**
 * Slice storing current report data of the system
 */
export const reportsSlice = createSlice({
   name: 'reports',
   initialState: INITIAL_STATE,
   reducers: {
      updateReports(state, action: PayloadAction<ReportsStore>) {
         Object.assign(state, action.payload)
      },
      updateAgentsReports(state, action: PayloadAction<AgentStatisticReport[]>) {
         const reports = action.payload.filter((reports) => reports.type === AgentType.SCHEDULER)[0]?.reports
         const systemTraffic = reports
            ? (reports as AgentSchedulerStatisticReports).trafficReport
            : state.systemTrafficReport
         Object.assign(state, { ...state, agentsReports: action.payload, systemTrafficReport: systemTraffic })
      },
      updateClientsReports(state, action: PayloadAction<FetchClientReportsMessage>) {
         Object.assign(state, { ...state, ...action.payload })
      },
      updateNetworkReports(state, action: PayloadAction<FetchNetworkReportsMessage>) {
         Object.assign(state, { ...state, ...action.payload })
      },
      updateManagingReports(state, action: PayloadAction<FetchManagingReportsMessage>) {
         Object.assign(state, { ...state, ...action.payload })
      },
      updateSystemTime(state, action: PayloadAction<SystemTimeMessage>) {
         state.secondsPerHour = action.payload.secondsPerHour
         state.systemStartTime = action.payload.time
      },
      resetReports(state) {
         Object.assign(state, INITIAL_STATE)
      }
   }
})
