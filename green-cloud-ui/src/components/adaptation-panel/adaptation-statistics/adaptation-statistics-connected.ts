import { managingSystemSelect, RootState, selectAdaptationGoals } from '@store'
import { connect } from 'react-redux'
import { AdaptationStatistics } from './adaptation-statistics'

const mapStateToProps = (state: RootState) => {
   return {
      managingSystem: managingSystemSelect(state),
      adaptationGoals: selectAdaptationGoals(state),
   }
}

export default connect(mapStateToProps)(AdaptationStatistics)
