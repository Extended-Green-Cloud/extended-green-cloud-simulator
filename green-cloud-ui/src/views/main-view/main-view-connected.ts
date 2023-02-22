import { AppDispatch, cloudNetworkActions } from '@store'
import { MainView } from './main-view'
import { connect } from 'react-redux'

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      openServerConnection: () => dispatch(cloudNetworkActions.openServerConnection()),
   }
}

export default connect(null, mapDispatchToProps)(MainView)
