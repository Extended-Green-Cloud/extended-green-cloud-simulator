import React from 'react'
import { CURRENT_CLOUD_STATISTICS } from './cloud-config'
import { DetailsField } from 'components'
import { CloudNetworkStatistics } from '@types'

interface Props {
   cloudStatistics: CloudNetworkStatistics
}

/**
 * Component represents a panel gathering all statistics of cloud network
 *
 * @returns JSX Element
 */
export const CloudPanel = ({ cloudStatistics }: Props) => {
   const mapStatisticsToFields = () => {
      return CURRENT_CLOUD_STATISTICS.map((field) => {
         const { key, label } = field
         const value = { ...cloudStatistics }[key] ?? ''
         return <DetailsField {...{ label, value, key }} />
      })
   }

   return <div>{mapStatisticsToFields()}</div>
}
