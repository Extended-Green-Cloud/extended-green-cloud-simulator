import { AgentType, MessageType } from "types/enum";
import { CapacityMessage } from "./payload/capacity-message-payload";
import { RegisterAgent } from "./payload/register-agent-message-payload";

export type MessagePayload = {
    type: MessageType,
    agentType?: AgentType,
    agentName?: string,
    data?: string | string[] | number | boolean | CapacityMessage | RegisterAgent 
}