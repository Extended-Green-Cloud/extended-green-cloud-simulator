import { AppDispatch, RootState, cloudNetworkActions } from '@store'
import { MainView } from './main-view'
import { connect } from 'react-redux'
import { MenuTab } from '@types'
import { navigatorActions, selectSelectedTab } from 'store/navigator'

const mapStateToProps = (state: RootState) => {
   return {
      selectedTab: selectSelectedTab(state)
   }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      resetServerConnection: () => dispatch(cloudNetworkActions.resetServerConnection()),
      setSelectedTab: (tab: MenuTab) => dispatch(navigatorActions.setSelectedTab(tab))
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(MainView)
