import { styles } from './client-select-styles'
import { SingleValue } from 'react-select'
import { ClientAgent, ClientAgentStatus, DropdownOption } from '@types'
import { useEffect, useState } from 'react'
import { JOB_STATUS_MAP } from '../client-panel-config'
import FilterModal from './filter-modal/filter-modal'
import ClientDropdown from './client-dropdown/client-dropdown'
import { IconSettings } from '@assets'

interface Props {
   clients: ClientAgentStatus[]
   selectedClient: ClientAgent | null
   setSelectedClient: (client: string | null) => void
   updateClientData: () => void
}

/**
 * Component representing collapsible header containing client selector dropdown and associated to it filters
 *
 * @returns JSX Element
 */
const ClientStatisticsSelect = ({ clients, selectedClient, setSelectedClient, updateClientData }: Props) => {
   const { selectorContainer, iconContainer } = styles
   const [jobStatusMap, setJobStatusMap] = useState<DropdownOption[]>(JOB_STATUS_MAP)
   const [isOpen, setIsOpen] = useState<boolean>(false)

   useEffect(() => {
      if (clients.length === 0) {
         setSelectedClient(null)
      }
   }, [clients])

   const changeSelectedClient = (value: SingleValue<DropdownOption>) => {
      setSelectedClient(value?.label ?? null)
      updateClientData()
   }

   return (
      <div>
         <FilterModal
            {...{
               jobStatusMap,
               setJobStatusMap,
               isOpen,
               setIsOpen
            }}
         />
         <div style={selectorContainer}>
            <div style={iconContainer} onClick={() => setIsOpen(!isOpen)}>
               <IconSettings size="55px" className={'filter-button'} />
            </div>
            <ClientDropdown
               {...{
                  selectedClient,
                  clients,
                  jobStatusMap,
                  changeSelectedClient
               }}
            />
         </div>
      </div>
   )
}

export default ClientStatisticsSelect
