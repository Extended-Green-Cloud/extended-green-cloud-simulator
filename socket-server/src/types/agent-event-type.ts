import { POWER_SHORTAGE_STATE, EVENT_TYPE } from "../constants/constants";
export interface CommonAgentEvent {
	disabled: boolean;
	type: EVENT_TYPE;
	occurrenceTime: Date;
	data: any;
}
export interface PowerShortageEvent extends CommonAgentEvent {
	state: POWER_SHORTAGE_STATE;
}

export interface WeatherDropEvent extends CommonAgentEvent {}

export interface SwitchOnOffEvent extends CommonAgentEvent {
	isServerOn: boolean;
}
export interface ServerMaintenanceEvent extends CommonAgentEvent {
	hasStarted: boolean;
	hasError: boolean;
	sendNewData: boolean | null;
	processDataInServer: boolean | null;
	informationInManager: boolean | null;
	maintenanceCompleted: boolean | null;
}

export type AgentEvent = PowerShortageEvent | WeatherDropEvent | SwitchOnOffEvent | ServerMaintenanceEvent;
