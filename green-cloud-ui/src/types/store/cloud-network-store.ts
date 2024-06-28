export type CloudNetworkStore = {
   currClientsNo: number
   currActiveJobsNo: number
   currActiveInCloudJobsNo: number
   currPlannedJobsNo: number
   finishedJobsNo: number
   finishedJobsInCloudNo: number
   failedJobsNo: number
   allocationStrategy: string
   prioritizationStrategy: string
   allocationStepsNumber: number
   modifications: string[]
   isNetworkSocketConnected?: boolean | null
   isAgentSocketConnected?: boolean | null
   isClientSocketConnected?: boolean | null
   isAdaptationSocketConnected?: boolean | null
   connectionToast: boolean
}
