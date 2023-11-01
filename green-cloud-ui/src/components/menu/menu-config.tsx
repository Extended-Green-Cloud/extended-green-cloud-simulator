import { IconClients, IconCloudMenu, IconGear, IconNetwork, IconPen } from '@assets'
import { MenuTab } from '@types'

export const ICON_SIZE = '45px'
export const ICON_OFFSET = -70

export const MENU_BUTTONS = [
   {
      header: 'CLOUD SUMMARY',
      icon: <IconCloudMenu size={ICON_SIZE} />,
      iconOffset: ICON_OFFSET,
      id: MenuTab.CLOUD_SUMMARY
   },
   {
      header: 'AGENTS',
      icon: <IconNetwork size={ICON_SIZE} />,
      iconOffset: ICON_OFFSET,
      id: MenuTab.AGENTS
   },
   {
      header: 'CLIENTS',
      icon: <IconClients size={ICON_SIZE} />,
      iconOffset: ICON_OFFSET,
      id: MenuTab.CLIENTS
   },
   {
      header: 'ADAPTATION',
      icon: <IconGear size={ICON_SIZE} />,
      iconOffset: ICON_OFFSET,
      id: MenuTab.ADAPTATION
   },
   {
      header: 'CREATOR',
      icon: <IconPen size={ICON_SIZE} />,
      iconOffset: ICON_OFFSET,
      id: MenuTab.CREATOR
   }
]
