import { JobCreator } from '@types'
import { validateResources } from './resource-utils'

/**
 * Method verifies if provided client data is correct.
 * It returns message indicating potential error.
 *
 * @param jobData data to be verified
 * @returns error message
 */
export const validateNewClientData = (jobData: JobCreator) => {
   if (jobData.processorName === '') {
      return 'Processor name is empty. Type of the task must be specified.'
   }
   if (jobData.duration <= 0) {
      return 'Duration of task execution must be greater than 0.'
   }
   if (jobData.deadline < 0) {
      return 'Job execution deadline cannot be smaller than 0.'
   }
   if (jobData.steps.length === 0) {
      return 'At least 1 job execution step must be specified.'
   }
   if (jobData.steps.reduce((prev, step) => step.duration + prev, 0) !== jobData.duration * 60 * 60) {
      return 'Duration of job execution steps must be equal to the duration of entire task execution.'
   }

   const resourceVerification = validateResources(jobData.resources)
   if (resourceVerification !== '') {
      return `Some of the job resources were specified incorrectly: ${resourceVerification}`
   }

   const jobStepResourceVerification = jobData.steps
      .map((step) => ({
         name: step.name,
         validation: validateResources(step.requiredResources)
      }))
      .find((step) => step.validation !== '')

   if (jobStepResourceVerification) {
      return `Resources for job step ${jobStepResourceVerification.name} are incorrect: ${jobStepResourceVerification.validation}`
   }

   return ''
}
