import React, { useState } from 'react'

import './cloud-statistics/cloud-config'
import { styles } from './network-panel-styles'

import { Card } from '@components'
import CloudStatistics from './cloud-statistics/cloud-statistics'
import DoubleTabHeader from 'components/common/double-tab-header/double-tab-header'
import JobSchedule from './job-schedule/job-schedule'

const statisticsHeader = 'Cloud Statistics'
const jobSchedule = 'Scheduled Jobs'

/**
 * Component represents a panel gathering all infromations about cloud network
 *
 * @returns JSX Element
 */
const NetworkPanel = () => {
   const [isCloudStatistics, setIsCloudStatistics] = useState(true)

   return (
      <Card
         {...{
            header: (
               <DoubleTabHeader
                  {...{
                     firstTabTitle: statisticsHeader,
                     secondTabTitle: jobSchedule,
                     isFirstTabSelected: isCloudStatistics,
                     setIsFirstTabSelected: setIsCloudStatistics,
                  }}
               />
            ),
            containerStyle: styles.cloudContainer,
         }}
      >
         {isCloudStatistics ? <CloudStatistics /> : <JobSchedule />}
      </Card>
   )
}

export default NetworkPanel
