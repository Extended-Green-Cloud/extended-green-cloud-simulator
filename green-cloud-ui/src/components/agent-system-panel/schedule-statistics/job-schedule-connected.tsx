import { RootState, selectScheduler } from '@store'
import { connect } from 'react-redux'
import { JobSchedule } from './job-schedule'

const mapStateToProps = (state: RootState) => {
   return {
      scheduler: selectScheduler(state),
   }
}

export default connect(mapStateToProps)(JobSchedule)
