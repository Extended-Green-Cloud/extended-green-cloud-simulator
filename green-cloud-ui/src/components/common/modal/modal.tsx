import React from 'react'
import ReactModal from 'react-modal'
import { styles } from './modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
   children?: React.ReactNode | React.ReactNode[]
   header: React.ReactNode | string
   contentStyle?: React.CSSProperties
   isNested?: boolean
}

/**
 * Component represents a pop-up modal
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsMenuOpen] - function changing the state of the modal
 * @param {React.ReactNode | React.ReactNode[] | undefined}[children] - content of the modal
 * @param {React.ReactNode | string}[header] - title of the modal
 * @param {React.CSSProperties}[contentStyle] - optional modal style
 * @param {boolean}[isNested] - optional flag indicating if the modal is nested
 * @returns JSX Element
 */
const Modal = ({
   isOpen,
   setIsOpen,
   header,
   children,
   contentStyle,
   isNested,
}: Props) => {
   const { modalStyle, modalTitle, mainContainer, contentWrapper, nested } =
      styles
   const { content, overlay, ...otherStyles } = modalStyle
   const styleModal = {
      content: { ...contentStyle, ...content },
      overlay: { ...overlay, ...(isNested ? nested : undefined) },
      ...otherStyles,
   }

   return (
      <ReactModal
         {...{
            style: styleModal,
            isOpen,
            appElement: document.getElementById('root') as HTMLElement,
            onRequestClose: () => setIsOpen(false),
            shouldCloseOnOverlayClick: true,
         }}
      >
         <div style={contentWrapper}>
            <div style={modalTitle}>{header}</div>
            <div style={mainContainer}>{children}</div>
         </div>
      </ReactModal>
   )
}

export default Modal
