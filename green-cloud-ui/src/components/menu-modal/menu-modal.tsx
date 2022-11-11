import {
   agentsActions,
   cloudNetworkActions,
   useAppDispatch,
   useAppSelector,
} from '@store'
import React from 'react'
import ReactModal from 'react-modal'
import { styles } from './menu-modal-styles'
import './css/menu-button-styles.css'

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

   return (
      <ReactModal
         style={styles.modalStyle}
         isOpen={isMenuOpen}
         appElement={document.getElementById('root') as HTMLElement}
         onRequestClose={() => setIsMenuOpen(false)}
         shouldCloseOnOverlayClick={true}
      >
         <div style={styles.menuTitle}>
            <span>SERVER MENU</span>
            {getServerStateIndicator()}
         </div>
         <div style={styles.buttonWrapper}>
            <button className="button-banner" onClick={handleOnReset}>
               {'Reset simulation'.toUpperCase()}
            </button>
            <button
               className={'button-banner ' + serverConnectionButtonClass}
               onClick={handleOnStop}
            >
               {(isServerConnected
                  ? 'Disconnect server'
                  : 'Connect to server'
               ).toUpperCase()}
            </button>
         </div>
      </ReactModal>
   )
}

export default MenuModal
