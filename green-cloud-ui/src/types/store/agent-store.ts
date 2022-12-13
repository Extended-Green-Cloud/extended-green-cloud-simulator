import { SchedulerAgent } from 'types/agents/scheduler-agent'
import { Agent } from '../agents/agent-interface'

export type AgentStore = {
   scheduler: SchedulerAgent | null
   agents: Agent[]
   clients: Agent[]
   selectedAgent: string | null
   selectedClient: string | null
}
