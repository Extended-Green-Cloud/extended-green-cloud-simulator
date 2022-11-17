import { styles } from './client-dropdown-styles'
import Select, { SingleValue } from 'react-select'
import { ClientAgent } from '@types'
import { useMemo } from 'react'
import {
   AgentOption,
   CLIENTS_ORDER,
   GroupedAgentOption,
} from '../../client-statistics-config'
import { FilterOptionOption } from 'react-select/dist/declarations/src/filters'
import {
   convertJobStatus,
   JobStatusSelect,
} from '../../client-statistics-config'

const selectPlaceholder = 'Provide client name'
const selectNoOption = 'Client not found'
const selectNoClients = 'Client list is empty'

interface Props {
   selectedClient: ClientAgent | null
   setSelectedClient: (client: SingleValue<AgentOption>) => void
   clients: ClientAgent[]
   jobStatusMap: JobStatusSelect[]
}

/**
 * Component representing dropdown client selector
 *
 * @param {ClientAgent | null}[selectedAgent] - currently selected client
 * @param {func}[setSelectedClient] - function used to update currently selected client
 * @param {ClientAgent[]}[clients] - all clients
 * @param {JobStatusSelect[]}[jobStatusMap] - map of relevant job statuses
 * @returns JSX Element
 */
const ClientDropdown = ({
   selectedClient,
   setSelectedClient,
   clients,
   jobStatusMap,
}: Props) => {
   const { select, selectTheme } = styles
   const filteredClientsForJobs = () =>
      clients.filter(
         (client) =>
            jobStatusMap.find(
               (job) =>
                  job.jobStatus ===
                  convertJobStatus(client.jobStatusEnum.toString())
            )?.isSelected
      )

   const aggregateOptions = (prev: GroupedAgentOption[], curr: ClientAgent) => {
      const currJob = curr.jobStatusEnum.toString()
      const clientName = curr.name.toUpperCase()
      const prevGroup = prev.find((opt) => opt.label === currJob)
      const clientToPush = { label: clientName, value: curr }

      if (prevGroup) {
         prevGroup.options.push(clientToPush)
      } else {
         prev.push({ label: currJob, options: [clientToPush] })
      }
      return prev
   }

   const sortClients = (
      clientA: GroupedAgentOption,
      clientB: GroupedAgentOption
   ) =>
      CLIENTS_ORDER.indexOf(clientA.label) -
      CLIENTS_ORDER.indexOf(clientB.label)

   const selectData = useMemo(
      () =>
         filteredClientsForJobs()
            .reduce(
               (prev, curr) => aggregateOptions(prev, curr),
               [] as GroupedAgentOption[]
            )
            .sort((a, b) => sortClients(a, b)),
      [clients, jobStatusMap]
   )

   const customFilter = (
      option: FilterOptionOption<AgentOption>,
      inputValue: string
   ) => option.label.includes(inputValue)

   const handleOnChange = (value: SingleValue<AgentOption>) =>
      setSelectedClient(value)

   const handleNoOption = () =>
      clients.length !== 0 ? selectNoOption : selectNoClients

   return (
      <Select
         value={{
            value: selectedClient,
            label: selectedClient?.name ?? '',
         }}
         onChange={handleOnChange}
         placeholder={selectPlaceholder}
         noOptionsMessage={handleNoOption}
         styles={select}
         theme={selectTheme}
         options={selectData}
         maxMenuHeight={100}
         minMenuHeight={0}
         isSearchable={true}
         isClearable={true}
         isMulti={false}
         menuPortalTarget={document.getElementById('root')}
         menuPosition={'fixed'}
         filterOption={customFilter}
      />
   )
}

export default ClientDropdown
