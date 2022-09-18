import { AgentType, MessageType } from "types/enum";
import { CapacityMessage } from "./capacity-message-payload";
import { RegisterAgent } from "./register-agent-payload";

export type MessagePayload = {
    type: MessageType,
    agentType?: AgentType,
    agentName?: string,
    data?: string | string[] | number | boolean | CapacityMessage | RegisterAgent
}