import { styles } from './status-filter-box-styles'
import { Checkbox } from 'pretty-checkbox-react'
import '@djthoms/pretty-checkbox'
import { JobStatusSelect } from '../../client-statistics-config'
import Collapse from 'components/common/collapse/collapse'

interface Props {
   jobStatusMap: JobStatusSelect[]
   setJobStatusMap: React.Dispatch<React.SetStateAction<JobStatusSelect[]>>
}

/**
 * Component representing collapsible box containing job status filters
 *
 * @param {JobStatusSelect[]}[jobStatusMap] - map containing relevant job statuses
 * @param {React.Dispatch<React.SetStateAction<JobStatusSelect[]>>}[setJobStatusMap] - function used to update job statuses' properties
 * @returns JSX Element
 */
const StatusFilterBox = ({ jobStatusMap, setJobStatusMap }: Props) => {
   const { collapse, checkBox, checkContainer } = styles

   const handleSelectChange = (status: string) => {
      setJobStatusMap((prevState) =>
         prevState.map((jobStatus) => {
            return jobStatus.jobStatus === status ? { ...jobStatus, isSelected: !jobStatus.isSelected } : jobStatus
         })
      )
   }

   const generateCheckBoxFields = () =>
      jobStatusMap.map((entry) => {
         const { jobStatus, isSelected } = entry
         return (
            <Checkbox
               {...{
                  key: jobStatus,
                  value: jobStatus,
                  style: checkBox,
                  checked: isSelected,
                  color: 'success',
                  shape: 'curve',
                  animation: 'smooth',
                  onChange: () => handleSelectChange(jobStatus),
               }}
            >
               {jobStatus}
            </Checkbox>
         )
      })

   return (
      <Collapse {...{ title: 'DISPLAY FILTERS', triggerStyle: collapse }}>
         <div style={checkContainer}>{generateCheckBoxFields()}</div>
      </Collapse>
   )
}

export default StatusFilterBox
