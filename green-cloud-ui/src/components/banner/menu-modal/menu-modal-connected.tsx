import {
   agentsActions,
   AppDispatch,
   cloudNetworkActions,
   RootState,
   selectConnectionState,
} from '@store'
import { connect } from 'react-redux'
import { MenuModal } from './menu-modal'

const mapStateToProps = (state: RootState) => {
   return { isServerConnected: selectConnectionState(state) }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      resetCloudNetwork: () =>
         dispatch(cloudNetworkActions.resetCloudNetwork()),
      resetAgents: () => dispatch(agentsActions.resetAgents()),
      closeServerConnection: () =>
         dispatch(cloudNetworkActions.closeServerConnection()),
      openServerConnection: () =>
         dispatch(cloudNetworkActions.openServerConnection()),
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(MenuModal)
