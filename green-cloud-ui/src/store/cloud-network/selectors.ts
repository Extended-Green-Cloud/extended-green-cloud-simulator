/* eslint-disable @typescript-eslint/no-unused-vars */
import { createSelector } from '@reduxjs/toolkit'
import { CloudNetworkStatistics, MenuTab } from '@types'
import { navigatorSelect } from 'store/navigator'
import { RootState } from 'store/store'

const cloudNetworkSelect = (state: RootState) => state.cloudNetwork

/**
 * Method returns currect connection state
 */
export const selectConnectionState = createSelector(
   [cloudNetworkSelect, navigatorSelect],
   (cloudNetworkSelect, navigatorSelect) => {
      const selectedTab = navigatorSelect.selectedTab

      if (selectedTab === MenuTab.ADAPTATION) {
         return cloudNetworkSelect.isAdaptationSocketConnected
      } else if (selectedTab === MenuTab.AGENTS) {
         return cloudNetworkSelect.isAgentSocketConnected
      } else if (selectedTab === MenuTab.CLIENTS) {
         return cloudNetworkSelect.isClientSocketConnected
      } else {
         return cloudNetworkSelect.isNetworkSocketConnected
      }
   }
)

/**
 * Method returns current connection toast state
 */
export const selectConnectionToast = createSelector(
   [cloudNetworkSelect],
   (cloudNetworkSelect) => cloudNetworkSelect.connectionToast
)

/**
 * Method returns current connection state
 */
export const selectNetworkStatistics = createSelector(
   [cloudNetworkSelect],
   (cloudNetworkSelect): CloudNetworkStatistics => {
      const {
         isAdaptationSocketConnected,
         isAgentSocketConnected,
         isClientSocketConnected,
         isNetworkSocketConnected,
         ...statistics
      } = cloudNetworkSelect
      return statistics
   }
)
