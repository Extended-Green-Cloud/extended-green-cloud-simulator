import { styles } from './client-panel-styles'
import { ClientAgent, ClientAgentStatus, JobStatus } from '@types'
import SubtitleContainer from 'components/common/subtitle-container/subtitle-container'
import { useEffect, useState } from 'react'
import {
   CLIENT_STATISTICS_RESOURCES_MAPPER,
   CLIENT_STATISTIC_MAPS,
   ClientMapType,
   STATUS_COLOR
} from './client-panel-config'
import DetailsField from 'components/common/details-field/details-field'
import Badge from 'components/common/badge/badge'
import ClientStatisticsSelect from './client-select/client-select'
import { Button, Header } from 'components/common'
import ClientJobDurationModal from './client-job-duration-modal/client-job-duration-modal'
import { convertSecondsToString, convertTimeToString } from 'utils/time-utils'
import ClientJobStepModal from './client-job-step-modal/client-job-step-modal'
import { collectResourcesToMultiMap } from 'utils/resource-utils'
import MultiLevelDetailsField from 'components/common/multi-level-detils-field/multi-level-details-field'

const description = 'Select client from the list to display current job statistics'

interface Props {
   clients: ClientAgentStatus[]
   selectedClient: ClientAgent | null
   setSelectedClient: (client: string | null) => void
   updateClientData: () => void
}

/**
 * Component representing panel displaying details regarding network clients
 *
 * @returns JSX Element
 */
export const ClientPanel = ({ clients, selectedClient, setSelectedClient, updateClientData }: Props) => {
   const [isDurationOpen, setIsDurationOpen] = useState(false)
   const [isStepModalOpen, setIsStepModalOpen] = useState(false)
   const { clientContent, clientStatistics, configurationWrapper } = styles

   useEffect(() => {
      if (clients.length === 0) {
         setSelectedClient(null)
      }
   }, [clients])

   const durationButton = (
      <Button
         buttonClassName="small-gray-button predefined-width-button"
         title="STATUS DURATION"
         onClick={() => {
            setIsDurationOpen(true)
         }}
      />
   )

   const stepsButton = (
      <Button
         buttonClassName="small-gray-button predefined-width-button"
         title="JOB STEPS"
         onClick={() => {
            setIsStepModalOpen(true)
         }}
      />
   )

   const mapStatisticsType = (type: ClientMapType) => {
      if (selectedClient) {
         return CLIENT_STATISTIC_MAPS[type].map((field) => {
            const { key, label } = field
            const clientVal = {
               ...(selectedClient.job as any),
               executor: selectedClient?.executor ?? '',
               status: selectedClient.status as any,
               finalEndTime: selectedClient.finalEndTime as any,
               expectedExecutionTime: selectedClient.expectedExecutionTime as any,
               estimatedPrice: `${((selectedClient?.estimatedPrice ?? 0) as number).toFixed(2)} $`,
               finalPrice: `${((selectedClient?.finalPrice ?? 0) as number).toFixed(2)} $`,
               durationMap: selectedClient.durationMap as any,
               jobExecutionProportion: selectedClient.jobExecutionProportion as any
            }[key]
            const value = getClientValue(key, clientVal)
            const property = ['status', 'durationMap', 'steps'].includes(key) ? 'valueObject' : 'value'

            return key === 'resources' ? (
               <MultiLevelDetailsField {...{ detailsFieldMap: value }} />
            ) : (
               <DetailsField {...{ label, [property]: value, key }} />
            )
         })
      }
   }

   const getClientValue = (key: string, value: any) => {
      if (key === 'status') return <Badge text={value} color={STATUS_COLOR[value as JobStatus]} />
      if (key === 'durationMap') return durationButton
      if (key === 'steps') return stepsButton
      if (key === 'finalEndTime') return value ? convertTimeToString(value) : '-'
      if (key === 'jobExecutionProportion') return `${Math.round(value * 100)}%`
      if (key === 'duration') return convertSecondsToString(value)
      if (key === 'expectedExecutionTime') return convertSecondsToString(value)
      if (key === 'resources') return collectResourcesToMultiMap(value, CLIENT_STATISTICS_RESOURCES_MAPPER)
      return value
   }

   const getJobDurationModal = () => {
      if (selectedClient && selectedClient.durationMap) {
         return (
            <ClientJobDurationModal
               {...{
                  isOpen: isDurationOpen,
                  setIsOpen: setIsDurationOpen,
                  client: selectedClient
               }}
            />
         )
      }
   }

   const getStepModal = () => {
      if (selectedClient && selectedClient.durationMap) {
         return (
            <ClientJobStepModal
               {...{
                  isOpen: isStepModalOpen,
                  setIsOpen: setIsStepModalOpen,
                  jobSteps: selectedClient.job.steps
               }}
            />
         )
      }
   }

   const generateClientsStatistics = () =>
      (['HEADER', 'RESOURCES', 'TIMELINE', 'EXECUTION INFO'] as ClientMapType[]).map((type) => (
         <div>
            <div style={configurationWrapper}>
               {type !== 'HEADER' && <Header {...{ text: type }} />}
               {mapStatisticsType(type)}
            </div>
         </div>
      ))

   return (
      <div style={clientContent}>
         <ClientStatisticsSelect {...{ clients, selectedClient, setSelectedClient, updateClientData }} />
         {!selectedClient || clients.length === 0 ? (
            <SubtitleContainer text={description} />
         ) : (
            <>
               <div style={clientStatistics}>{generateClientsStatistics()}</div>
               {getJobDurationModal()}
               {getStepModal()}
            </>
         )}
      </div>
   )
}
