import { RootState, selectNetworkStatistics } from '@store'
import { connect } from 'react-redux'
import { StrategyPanel } from './strategy-panel'

const mapStateToProps = (state: RootState) => {
   return {
      cloudStatistics: selectNetworkStatistics(state)
   }
}

export default connect(mapStateToProps)(StrategyPanel)
