import { createSelector } from '@reduxjs/toolkit'
import { SchedulerAgent } from '@types'
import { RootState } from 'store/store'

export const agentSelector = (state: RootState) => state.agents

/**
 * Method returns selected network agent
 */
export const selectChosenNetworkAgent = createSelector([agentSelector], (agentSelector) => agentSelector.agentData)

/**
 * Method returns selected network agent id
 */
export const selectChosenNetworkAgentId = createSelector(
   [agentSelector],
   (agentSelector) => agentSelector.selectedAgent
)
/**
 * Method returns scheduled jobs
 */
export const selectScheduledJobs = createSelector(
   [agentSelector],
   (agentSelector) =>
      (agentSelector.agents.find((agent) => agent.name === agentSelector.selectedAgent) as SchedulerAgent)
         .scheduledJobs ?? null
)
