import React from 'react'

import { CURRENT_CLOUD_STATISTICS } from './cloud-config'

import { useAppSelector } from '@store'
import { DetailsField } from 'components'

/**
 * Component represents a panel gathering all statistics of cloud network
 *
 * @returns JSX Element
 */
const CloudStatistics = () => {
   const cloudNetworkState = useAppSelector((state) => state.cloudNetwork)

   const mapStatisticsToFields = () => {
      return CURRENT_CLOUD_STATISTICS.map((field) => {
         const { key, label } = field
         const value = { ...cloudNetworkState }[key] ?? ''
         return <DetailsField {...{ label, value, key }} />
      })
   }

   return <div>{mapStatisticsToFields()}</div>
}

export default CloudStatistics
