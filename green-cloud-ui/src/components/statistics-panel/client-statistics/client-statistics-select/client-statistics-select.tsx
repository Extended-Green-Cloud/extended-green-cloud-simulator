import { styles } from './client-statistics-select-styles'
import { SingleValue } from 'react-select'
import { agentsActions } from '@store'
import { ClientAgent } from '@types'
import { useEffect, useState } from 'react'
import {
   AgentOption,
   JobStatusSelect,
   JOB_STATUS_MAP,
} from '../client-statistics-config'
import '@djthoms/pretty-checkbox'
import StatusFilterBox from './status-filter-box/status-filter-box'
import ClientDropdown from './client-dropdown/client-dropdown'
import { AnyAction, Dispatch } from 'redux'
import Collapse from 'components/common/collapse/collapse'

interface Props {
   clients: ClientAgent[]
   selectedClient: ClientAgent | null
   dispatch: Dispatch<AnyAction>
}

const collapseOpen = 'Hide Client Selector'
const collapseClose = 'Display Client Selector'

/**
 * Component representing collapsible header containing client selector dropdown and associated to it filters
 *
 * @returns JSX Element
 */
const ClientStatisticsSelect = ({
   clients,
   selectedClient,
   dispatch,
}: Props) => {
   const { collapseStyle, collapseOpenStyle, collapseCloseStyle } = styles
   const [jobStatusMap, setJobStatusMap] =
      useState<JobStatusSelect[]>(JOB_STATUS_MAP)

   const styleOpen = { ...collapseStyle, ...collapseOpenStyle }
   const styleClosed = { ...collapseStyle, ...collapseCloseStyle }

   useEffect(() => {
      if (clients.length === 0) {
         setSelectedClient(null)
      }
   }, [clients, dispatch])

   const setSelectedClient = (value: SingleValue<AgentOption>) => {
      dispatch(agentsActions.setSelectedClient(value?.label ?? null))
   }

   return (
      <Collapse
         {...{
            title: collapseOpen,
            titleClosed: collapseClose,
            triggerStyle: styleOpen,
            triggerClosedStyle: styleClosed,
         }}
      >
         <StatusFilterBox {...{ jobStatusMap, setJobStatusMap }} />
         <ClientDropdown
            {...{ selectedClient, clients, jobStatusMap, setSelectedClient }}
         />
      </Collapse>
   )
}

export default ClientStatisticsSelect
