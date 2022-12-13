import { RootState, selectAdaptationGoals } from '@store'
import { connect } from 'react-redux'
import { AdaptationGoals } from './adaptation-goals'

const mapStateToProps = (state: RootState) => {
   return {
      goals: selectAdaptationGoals(state),
   }
}

export default connect(mapStateToProps)(AdaptationGoals)
