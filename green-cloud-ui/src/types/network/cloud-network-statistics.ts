export type CloudNetworkStatistics = {
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
   connectionToast: boolean
}
