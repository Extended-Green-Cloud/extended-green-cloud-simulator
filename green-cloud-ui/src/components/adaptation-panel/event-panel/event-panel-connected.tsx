import {
   agentsActions,
   AppDispatch,
   RootState,
   selectChosenNetworkAgent,
} from '@store'
import { PowerShortageEventData } from '@types'
import { connect } from 'react-redux'
import { EventPanel } from './event-panel'

const mapStateToProps = (state: RootState) => {
   return {
      selectedAgent: selectChosenNetworkAgent(state),
   }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      triggerPowerShortage: (data: PowerShortageEventData) =>
         dispatch(agentsActions.triggerPowerShortage(data)),
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(EventPanel)
