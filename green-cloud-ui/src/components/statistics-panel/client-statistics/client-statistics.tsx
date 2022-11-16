import { styles } from './client-statistics-styles'
import { agentsActions, useAppDispatch, useAppSelector } from '@store'
import { AgentStore, ClientAgent } from '@types'
import SubtitleContainer from 'components/common/subtitle-container/subtitle-container'
import { useEffect } from 'react'
import { CLIENT_STATISTICS } from './client-statistics-config'
import DetailsField from 'components/common/details-field/details-field'
import Badge from 'components/common/badge/badge'
import '@djthoms/pretty-checkbox'
import ClientStatisticsSelect from './client-statistics-select/client-statistics-select'

const description =
   'Select client from the list to diplay current job statistics'

/**
 * Component representing panel displaying details regarding network clients
 *
 * @returns JSX Element
 */
const ClientPanel = () => {
   const dispatch = useAppDispatch()
   const agentState: AgentStore = useAppSelector((state) => state.agents)

   const clients = agentState.clients as ClientAgent[]
   const selectedClient =
      clients.find(
         (agent) => agent.name.toUpperCase() === agentState.selectedClient
      ) ?? null
   const { clientContent, clientStatistics } = styles

   useEffect(() => {
      if (clients.length === 0) {
         dispatch(agentsActions.setSelectedClient(null))
      }
   }, [clients, dispatch])

   const generateClientInfo = () => {
      if (selectedClient) {
         return CLIENT_STATISTICS.map((field) => {
            const { key, label } = field
            const clientVal = { ...(selectedClient as any) }[key]
            const value =
               key === 'jobStatusEnum' ? <Badge text={clientVal} /> : clientVal
            const property = key === 'jobStatusEnum' ? 'valueObject' : 'value'

            return <DetailsField {...{ label, [property]: value, key }} />
         })
      }
   }

   return (
      <div style={clientContent}>
         <ClientStatisticsSelect {...{ clients, selectedClient, dispatch }} />
         {!selectedClient || clients.length === 0 ? (
            <SubtitleContainer text={description} />
         ) : (
            <div style={clientStatistics}>
               <DetailsField
                  {...{ label: selectedClient.name, isHeader: true }}
               />
               {generateClientInfo()}
            </div>
         )}
      </div>
   )
}

export default ClientPanel
