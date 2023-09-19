export type CloudNetworkStore = {
   currClientsNo: number
   currActiveJobsNo: number
   currActiveInCloudJobsNo: number
   currPlannedJobsNo: number
   finishedJobsNo: number
   finishedJobsInCloudNo: number
   failedJobsNo: number
   isNetworkSocketConnected?: boolean | null
   isAgentSocketConnected?: boolean | null
   isClientSocketConnected?: boolean | null
   isAdaptationSocketConnected?: boolean | null
   connectionToast: boolean
}
