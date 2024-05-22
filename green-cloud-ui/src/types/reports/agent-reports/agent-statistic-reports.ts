import { AgentType } from 'types/enum'
import { AgentRegionalManagerStatisticReports } from './agent-cloud-network-reports'
import { AgentGreenSourceStatisticReports } from './agent-green-source-reports'
import { AgentCentralManagerStatisticReports } from './agent-central-manager-reports'
import { AgentServerStatisticReports } from './agent-server-reports'
import { ReportEvents } from '../report-events'

export interface AgentStatisticReport {
   name: string
   type: AgentType
   reports:
      | AgentRegionalManagerStatisticReports
      | AgentCentralManagerStatisticReports
      | AgentServerStatisticReports
      | AgentGreenSourceStatisticReports
   events: ReportEvents[]
}
