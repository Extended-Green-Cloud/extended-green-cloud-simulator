export type CloudNetworkStore = {
   currClientsNo: number
   currActiveJobsNo: number
   currActiveJobsInCloudNo: number
   currPlannedJobsNo: number
   finishedJobsNo: number
   failedJobsNo: number
   finishedJobsInCloudNo: number
   isNetworkSocketConnected?: boolean | null
   isAgentSocketConnected?: boolean | null
   isClientSocketConnected?: boolean | null
   isAdaptationSocketConnected?: boolean | null
   connectionToast: boolean
}
