import { IconInfo } from '@assets'
import Modal from 'components/common/modal/modal'
import React from 'react'
import { styles } from './system-indicator-modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
}

const header = 'System Quality Indicator'
const description =
   'System Quality Indicator is a numerical representation of how well the system is operating based on the currently established quality properties'

/**
 * Component represents a pop-up modal with description of system indicator
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @returns JSX Element
 */
const SystemIndicatorModal = ({ isOpen, setIsOpen }: Props) => {
   const { modalStyle, iconStyle, title, content } = styles

   const headerContent = (
      <>
         <IconInfo {...iconStyle} />
         <span style={title}>{header.toUpperCase()}</span>
      </>
   )

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            header: headerContent,
            contentStyle: modalStyle,
         }}
      >
         <div style={content}>{description}</div>
      </Modal>
   )
}

export default SystemIndicatorModal
