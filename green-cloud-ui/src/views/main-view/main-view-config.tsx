import { MenuTab } from '@types'

export type ViewTabConfig = {
   [key in MenuTab]: {
      useDoublePanelView: boolean
   }
}

export const VIEW_TABS: ViewTabConfig = {
   [MenuTab.ADAPTATION]: {
      useDoublePanelView: true
   },
   [MenuTab.AGENTS]: {
      useDoublePanelView: true
   },
   [MenuTab.CLIENTS]: {
      useDoublePanelView: true
   },
   [MenuTab.CLOUD_SUMMARY]: {
      useDoublePanelView: true
   },
   [MenuTab.CREATOR]: {
      useDoublePanelView: false
   }
}
