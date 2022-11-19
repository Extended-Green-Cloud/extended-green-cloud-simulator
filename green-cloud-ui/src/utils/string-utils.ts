export const parseSplitJobId = (jobId: string) => {
   const originalJob = retrieveOriginalJobId(jobId)
   const splitId = jobId.split('#part')[1]
   const splitString = ['(PART ', splitId, ')'].join('')
   return [originalJob, splitString].join(' ')
}

export const retrieveOriginalJobId = (jobId: string) => {
   return jobId.split('#')[0]
}
