import { AGENT_TYPES } from "../../../constants";
import { AgentEvent } from "../../../types";

export interface CommonAgent {
	type: AGENT_TYPES;
	name: string;
	events: AgentEvent[];
	isActive: boolean;
	adaptation: string;
}
