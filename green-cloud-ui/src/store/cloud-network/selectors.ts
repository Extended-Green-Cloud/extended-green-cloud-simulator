import { createSelector } from '@reduxjs/toolkit'
import { RootState } from 'store/store'

const cloudNetworkSelect = (state: RootState) => state.cloudNetwork

/**
 * Method returns currect connection state
 */
export const selectConnectionState = createSelector(
   [cloudNetworkSelect],
   (cloudNetworkSelect) => cloudNetworkSelect.isServerConnected
)

/**
 * Method returns currect connection state
 */
export const selectNetworkStatistics = createSelector(
   [cloudNetworkSelect],
   (cloudNetworkSelect) => {
      const { isServerConnected, ...statistics } = cloudNetworkSelect // eslint-disable-line @typescript-eslint/no-unused-vars
      return statistics
   }
)
