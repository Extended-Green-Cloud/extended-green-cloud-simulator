import { AdaptationLog } from '@types'
import Modal from 'components/common/modal/modal'
import React from 'react'
import LogEntry from '../log-entry/log-entry'
import { styles } from './full-log-modal-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
   sortedLogs: AdaptationLog[]
}

const header = 'Adaptation logs'

/**
 * Component represents a pop-up modal displaying all adaptation logs
 *
 * @param {boolean}[isOpen] - flag indicating if the modal is currently open
 * @param {func}[setIsOpen] - function changing the state of the modal
 * @param {AdaptationLog[]}[sortedLogs] - sorted by date adaptation logs
 * @returns JSX Element
 */
const FullLogModal = ({ isOpen, setIsOpen, sortedLogs }: Props) => {
   const { modalStyle } = styles

   const generateAdaptationLogs = () =>
      sortedLogs.map((log) => <LogEntry {...{ adaptationLog: log }} />)

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            header: header.toUpperCase(),
            contentStyle: modalStyle,
         }}
      >
         {generateAdaptationLogs()}
      </Modal>
   )
}

export default FullLogModal
