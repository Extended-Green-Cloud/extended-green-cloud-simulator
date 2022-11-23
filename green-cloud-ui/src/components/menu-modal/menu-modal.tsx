import {
   agentsActions,
   cloudNetworkActions,
   useAppDispatch,
   useAppSelector,
} from '@store'
import React from 'react'
import { styles } from './menu-modal-styles'
import { Modal, Button } from 'components'

interface Props {
   isMenuOpen: boolean
   setIsMenuOpen: (state: boolean) => void
}

/**
 * Component represents a server menu pop-up modal
 *
 * @param {boolean}[isMenuOpen] - flag indicating if the menu is currently open
 * @param {func}[setIsMenuOpen] - function changing the state of the menu modal
 * @returns JSX Element
 */
const MenuModal = ({ isMenuOpen, setIsMenuOpen }: Props) => {
   const { modalStyle } = styles
   const { isServerConnected } = useAppSelector((state) => state.cloudNetwork)
   const dispatch = useAppDispatch()
   const serverConnectionButtonClass = isServerConnected
      ? ''
      : 'button-disconnected'

   const handleOnReset = () => {
      dispatch(cloudNetworkActions.resetCloudNetwork())
      dispatch(agentsActions.resetAgents())
   }

   const handleOnStop = () => {
      if (isServerConnected) {
         dispatch(cloudNetworkActions.finishNetworkStateFetching())
         dispatch(agentsActions.resetAgents())
      } else {
         dispatch(cloudNetworkActions.startNetworkStateFetching())
      }
   }

   const getServerStateIndicator = () => {
      const fill = isServerConnected ? '#179d10' : '#dd3030'
      return (
         <svg viewBox="0 0 20 20" height="1.2rem">
            <circle cx="15" cy="10" r="4" {...{ fill }} />
         </svg>
      )
   }

   const header = (
      <>
         <span>SERVER MENU</span>
         {getServerStateIndicator()}
      </>
   )

   return (
      <Modal
         {...{
            isOpen: isMenuOpen,
            setIsOpen: setIsMenuOpen,
            header,
            contentStyle: modalStyle,
         }}
      >
         <Button
            {...{
               buttonClassName: 'button-banner',
               onClick: handleOnReset,
               title: 'RESET SIMULATION',
            }}
         />
         <Button
            {...{
               buttonClassName: 'button-banner ' + serverConnectionButtonClass,
               onClick: handleOnStop,
               title: isServerConnected
                  ? 'DISCONNECT SERVER'
                  : 'CONNECT TO SERVER',
            }}
         />
      </Modal>
   )
}

export default MenuModal
