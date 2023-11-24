import { AppDispatch, RootState, cloudNetworkActions, selectAgents } from '@store'
import { connect } from 'react-redux'
import { CreatorPanel } from './creator-panel'
import { AgentType, ClientCreator, GreenSourceCreator, ServerCreator } from '@types'

const mapStateToProps = (state: RootState) => {
   return {
      agents: selectAgents(state)
   }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      createClient: (clientData: ClientCreator) =>
         dispatch(cloudNetworkActions.createAgent({ clientData, agentType: AgentType.CLIENT })),
      createGreenSource: (greenSourceData: GreenSourceCreator) =>
         dispatch(cloudNetworkActions.createAgent({ greenSourceData, agentType: AgentType.GREEN_ENERGY })),
      createServer: (serverData: ServerCreator) =>
         dispatch(cloudNetworkActions.createAgent({ serverData, agentType: AgentType.SERVER }))
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(CreatorPanel)
