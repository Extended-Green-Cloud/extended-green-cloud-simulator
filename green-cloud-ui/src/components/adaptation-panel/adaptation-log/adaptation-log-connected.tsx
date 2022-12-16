import { RootState, selectSortedAdaptationLogs } from '@store'
import { connect } from 'react-redux'
import { AdaptationLogPanel } from './adaptation-log'

const mapStateToProps = (state: RootState) => {
   return {
      sortedLogs: selectSortedAdaptationLogs(state),
   }
}

export default connect(mapStateToProps)(AdaptationLogPanel)
