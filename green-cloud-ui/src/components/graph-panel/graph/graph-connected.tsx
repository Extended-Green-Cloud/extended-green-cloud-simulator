import {
   agentsActions,
   AppDispatch,
   RootState,
   selectExistingConnections,
   selectNetworkNodes,
   selectScheduler,
} from '@store'
import { connect } from 'react-redux'
import { DisplayGraph } from './graph'

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      setSelectedAgent: (id: string) => dispatch(agentsActions.setSelectedAgent(id)),
   }
}

const mapStateToProps = (state: RootState) => {
   return {
      nodes: selectNetworkNodes(state),
      connections: selectExistingConnections(state),
      scheduler: selectScheduler(state),
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(DisplayGraph)
