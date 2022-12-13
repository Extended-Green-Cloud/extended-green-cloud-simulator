import {
   agentsActions,
   AppDispatch,
   RootState,
   selectChosenClient,
   selectClients,
} from '@store'
import { connect } from 'react-redux'
import { ClientPanel } from './client-statistics'

const mapStateToProps = (state: RootState) => {
   return {
      clients: selectClients(state),
      selectedClient: selectChosenClient(state),
   }
}

const mapDispatchToProps = (dispatch: AppDispatch) => {
   return {
      setSelectedClient: (client: string | null) =>
         dispatch(agentsActions.setSelectedClient(client)),
   }
}

export default connect(mapStateToProps, mapDispatchToProps)(ClientPanel)
