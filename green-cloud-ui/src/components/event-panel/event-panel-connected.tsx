import { agentsActions, AppDispatch, RootState, selectChosenNetworkAgent } from '@store'
import { PowerShortageEventData, ServerMaintenanceEventData, SwitchOnOffEventData, WeatherDropEventData } from '@types'
import { connect } from 'react-redux'
import { EventPanel } from './event-panel'

const mapStateToProps = (state: RootState) => {
   return {
      selectedAgent: selectChosenNetworkAgent(state)
   }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      triggerPowerShortage: (data: PowerShortageEventData) => dispatch(agentsActions.triggerPowerShortage(data)),
      triggerWeatherDrop: (data: WeatherDropEventData) => dispatch(agentsActions.triggerWeatherDrop(data)),
      switchServerState: (data: SwitchOnOffEventData) => dispatch(agentsActions.triggerSwitchOnOfServer(data)),
      triggerServerMaintenance: (data: ServerMaintenanceEventData) =>
         dispatch(agentsActions.triggerServerMaintenance(data)),
      resetServerMaintenance: (agentName: string) => dispatch(agentsActions.resetServerMaintenance(agentName))
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(EventPanel)
