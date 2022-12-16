import { RootState, selectChosenNetworkAgent } from '@store'
import { connect } from 'react-redux'
import { AgentSystemPanel } from './agent-system-panel'

const mapStateToProps = (state: RootState) => {
   return {
      selectedAgent: selectChosenNetworkAgent(state),
   }
}

export default connect(mapStateToProps)(AgentSystemPanel)
