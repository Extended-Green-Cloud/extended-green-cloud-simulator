import { Agent, GreenSourceCreator, JobCreator, ServerCreator } from '@types'
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

/**
 * Method verifies if provided green source data is correct.
 * It returns message indicating potential error.
 *
 * @param greenSourceData data to be verified
 * @param agents agents present in the system
 * @returns error message
 */
export const validateGreenSourceData = (greenSourceData: GreenSourceCreator, agents: Agent[]) => {
   if (greenSourceData.name === '') {
      return 'Green Source name cannot be empty.'
   }
   if (agents.find((agent) => agent.name === greenSourceData.name)) {
      return 'Provided name already exists. Name of the Green Source must be unique with respect to already existing agents.'
   }
   if (greenSourceData.latitude > 90 || greenSourceData.latitude < -90) {
      return 'Latitude must have a value between [-90, 90].'
   }
   if (greenSourceData.longitude > 190 || greenSourceData.longitude < -180) {
      return 'Longitude must have a value between [-90, 90].'
   }
   if (greenSourceData.pricePerPowerUnit < 0) {
      return 'Price per power unit must be at least 0.'
   }
   if (greenSourceData.weatherPredictionError < 0) {
      return 'Weather prediction error cannot be negative.'
   }
   if (greenSourceData.maximumCapacity <= 0) {
      return 'Maximum capacity of the Green Source must be greater than 0.'
   }

   return ''
}

/**
 * Method verifies if provided server data is correct.
 * It returns message indicating potential error.
 *
 * @param greenSourceData data to be verified
 * @param agents agents present in the system
 * @returns error message
 */
export const validateServerData = (serverData: ServerCreator, agents: Agent[]) => {
   if (serverData.name === '') {
      return 'Server name cannot be empty.'
   }
   if (agents.find((agent) => agent.name === serverData.name)) {
      return 'Provided name already exists. Name of the Server must be unique with respect to already existing agents.'
   }
   if (serverData.idlePower < 0) {
      return 'Idle power consumption cannot be smaller than 0.'
   }
   if (serverData.maxPower <= 0) {
      return 'Maximal server power consumption must be greater than 0.'
   }
   if (serverData.jobProcessingLimit <= 0) {
      return 'Job processing limit must be greater than 0.'
   }
   if (serverData.price < 0) {
      return 'Price per power unit cannot be smaller than 0.'
   }

   const verificationResources = validateResources(serverData.resources)
   if (verificationResources !== '') {
      return `Specified Server resources are incorrect: ${verificationResources}`
   }

   return ''
}
