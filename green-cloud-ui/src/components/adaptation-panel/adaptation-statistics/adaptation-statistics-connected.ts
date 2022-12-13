import { managingSystemSelect, RootState } from '@store'
import { connect } from 'react-redux'
import { AdaptationStatistics } from './adaptation-statistics'

const mapStateToProps = (state: RootState) => {
   return {
      managingSystem: managingSystemSelect(state),
   }
}

export default connect(mapStateToProps)(AdaptationStatistics)
