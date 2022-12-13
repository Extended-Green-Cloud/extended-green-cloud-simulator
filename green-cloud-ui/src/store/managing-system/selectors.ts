import { createSelector } from '@reduxjs/toolkit'
import { RootState } from 'store/store'

/**
 * Method returns managing system state
 */
export const managingSystemSelect = (state: RootState) => state.managingSystem

/**
 * Method returns adaptation logs sorted by time
 */
export const selectSortedAdaptationLogs = createSelector(
   [managingSystemSelect],
   (managingSystemSelect) =>
      [...managingSystemSelect.adaptationLogs].sort(
         (log1, log2) => log2.time - log1.time
      )
)

/**
 * Method returns adaptation goals
 */
export const selectAdaptationGoals = createSelector(
   [managingSystemSelect],
   (managingSystemSelect) => managingSystemSelect.adaptationGoals
)
