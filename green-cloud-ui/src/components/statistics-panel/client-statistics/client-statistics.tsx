import { styles } from './client-statistics-styles'
import { agentsActions, useAppDispatch, useAppSelector } from '@store'
import { AgentStore, ClientAgent } from '@types'
import SubtitleContainer from 'components/common/subtitle-container/subtitle-container'
import { useEffect, useState } from 'react'
import { CLIENT_STATISTICS } from './client-statistics-config'
import DetailsField from 'components/common/details-field/details-field'
import Badge from 'components/common/badge/badge'
import ClientStatisticsSelect from './client-statistics-select/client-statistics-select'
import { ModalButton } from 'components/common'
import ClientSplitJobModal from './client-split-job-modal/client-split-job-modal'

const description =
   'Select client from the list to diplay current job statistics'

/**
 * Component representing panel displaying details regarding network clients
 *
 * @returns JSX Element
 */
const ClientPanel = () => {
   const [isOpen, setIsOpen] = useState(false)
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
            const clientVal = {
               ...(selectedClient.job as any),
               status: selectedClient.status as any,
            }[key]
            const value =
               key === 'status' ? <Badge text={clientVal} /> : clientVal
            const property = key === 'status' ? 'valueObject' : 'value'

            return <DetailsField {...{ label, [property]: value, key }} />
         })
      }
   }

   const getModalButton = (
      <ModalButton
         {...{
            buttonClassName: 'small-green-button',
            setIsOpen,
            title: 'SPLIT JOBS',
         }}
      />
   )

   const getClientHeader = () => {
      const { isSplit } = selectedClient as ClientAgent
      return isSplit ? (
         <DetailsField
            {...{
               label: selectedClient?.name ?? '',
               isHeader: true,
               valueObject: getModalButton,
            }}
         />
      ) : (
         <DetailsField {...{ label: selectedClient?.name, isHeader: true }} />
      )
   }

   const getSplitJobModal = () => {
      if (selectedClient?.isSplit) {
         return (
            <ClientSplitJobModal
               {...{
                  isOpen,
                  setIsOpen,
                  client: selectedClient,
               }}
            />
         )
      }
   }

   return (
      <div style={clientContent}>
         <ClientStatisticsSelect {...{ clients, selectedClient, dispatch }} />
         {!selectedClient || clients.length === 0 ? (
            <SubtitleContainer text={description} />
         ) : (
            <>
               <div style={clientStatistics}>
                  {getClientHeader()}
                  {generateClientInfo()}
               </div>
               {getSplitJobModal()}
            </>
         )}
      </div>
   )
}

export default ClientPanel
