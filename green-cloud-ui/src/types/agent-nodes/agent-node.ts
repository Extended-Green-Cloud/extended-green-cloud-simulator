import { CloudNetworkNode } from './cloud-network-node'
import { CommonAgentNodeInterface } from './common'
import { GreenEnergyNode } from './green-energy-node'
import { ServerNode } from './server-agent-node'

export type AgentNode =
   | CloudNetworkNode
   | ServerNode
   | GreenEnergyNode
   | CommonAgentNodeInterface
