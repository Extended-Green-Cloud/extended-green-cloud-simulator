import { AppDispatch, RootState, cloudNetworkActions, selectAgents } from '@store'
import { connect } from 'react-redux'
import { CreatorPanel } from './creator-panel'
import { AgentType, JobCreator } from '@types'

const mapStateToProps = (state: RootState) => {
   return {
      agents: selectAgents(state)
   }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      createClient: (jobData: JobCreator) =>
         dispatch(cloudNetworkActions.createAgent({ jobData, agentType: AgentType.CLIENT }))
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(CreatorPanel)
