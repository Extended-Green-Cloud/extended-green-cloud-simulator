import React, { useState } from 'react'
import { styles } from './menu-modal-styles'
import { Modal, Button } from 'components'
import ThirdPartyLibraries from './third-party-libraries/third-party-libraries'

interface Props {
   isMenuOpen: boolean
   setIsMenuOpen: (state: boolean) => void
   isServerConnected?: boolean
   resetCloudNetwork: () => void
   resetAgents: () => void
   closeServerConnection: () => void
   openServerConnection: () => void
}

/**
 * Component represents a server menu pop-up modal
 *
 * @param {boolean}[isMenuOpen] - flag indicating if the menu is currently open
 * @param {func}[setIsMenuOpen] - function changing the state of the menu modal
 * @returns JSX Element
 */
export const MenuModal = ({
   isMenuOpen,
   setIsMenuOpen,
   isServerConnected,
   resetCloudNetwork,
   resetAgents,
   closeServerConnection,
   openServerConnection,
}: Props) => {
   const [isThirdPartyOpen, setIsThirdPartyOpen] = useState(false)
   const { modalStyle } = styles
   const serverConnectionButtonClass = isServerConnected
      ? ''
      : 'button-disconnected'

   const handleOnReset = () => {
      resetCloudNetwork()
      resetAgents()
      window.location.reload()
   }

   const handleOnStop = () => {
      if (isServerConnected) {
         closeServerConnection()
         resetAgents()
      } else {
         openServerConnection()
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
         <Button
            {...{
               buttonClassName: 'button-banner button-banner-gray',
               onClick: () => setIsThirdPartyOpen(true),
               title: 'THIRD-PARTY LIBRARIES',
            }}
         />
         <ThirdPartyLibraries
            {...{ isOpen: isThirdPartyOpen, setIsOpen: setIsThirdPartyOpen }}
         />
      </Modal>
   )
}
