import { PowerShortageEvent, ServerMaintenanceEvent, SwitchOnOffEvent, WeatherDropEvent } from "../types";

enum JOB_STATUSES {
	CREATED = "CREATED",
	PROCESSED = "PROCESSED",
	IN_PROGRESS = "IN PROGRESS",
	IN_PROGRESS_CLOUD = "IN PROGRESS IN CLOUD",
	DELAYED = "DELAYED",
	FINISHED = "FINISHED",
	ON_BACK_UP = "ON BACK UP",
	ON_HOLD = "ON HOLD",
	REJECTED = "REJECTED",
	FAILED = "FAILED",
}

enum AGENT_TYPES {
	REGIONAL_MANAGER = "REGIONAL_MANAGER",
	CLIENT = "CLIENT",
	SERVER = "SERVER",
	GREEN_ENERGY = "GREEN_ENERGY",
	MONITORING = "MONITORING",
	SCHEDULER = "SCHEDULER",
}

enum POWER_SHORTAGE_STATE {
	ACTIVE = "ACTIVE",
	INACTIVE = "INACTIVE",
}

enum ENERGY_TYPE {
	SOLAR = "SOLAR",
	WIND = "WIND",
}

enum EVENT_TYPE {
	POWER_SHORTAGE_EVENT = "POWER_SHORTAGE_EVENT",
	WEATHER_DROP_EVENT = "WEATHER_DROP_EVENT",
	SWITCH_ON_OFF_EVENT = "SWITCH_ON_OFF_EVENT",
	SWITCH_ON_EVENT = "SWITCH_ON_EVENT",
	SWITCH_OFF_EVENT = "SWITCH_OFF_EVENT",
	SERVER_MAINTENANCE_EVENT = "SERVER_MAINTENANCE_EVENT",
	CLIENT_CREATION_EVENT = "CLIENT_CREATION_EVENT",
	GREEN_SOURCE_CREATION_EVENT = "GREEN_SOURCE_CREATION_EVENT",
	SERVER_CREATION_EVENT = "SERVER_CREATION_EVENT",
	AGENT_CONNECTION_CHANGE = "AGENT_CONNECTION_CHANGE",
}

const WELCOMING_MESSAGE = {
	type: "SOCKET_CONNECTED",
	data: "Connection to the socket established successfully",
};

const ROUTE_TYPES = {
	FRONT: "/frontend",
};

const INITIAL_WEATHER_DROP_STATE: WeatherDropEvent = {
	disabled: false,
	type: EVENT_TYPE.WEATHER_DROP_EVENT,
	occurrenceTime: null,
	data: null,
};

const INITIAL_POWER_SHORTAGE_STATE: PowerShortageEvent = {
	state: POWER_SHORTAGE_STATE.ACTIVE,
	disabled: false,
	type: EVENT_TYPE.POWER_SHORTAGE_EVENT,
	occurrenceTime: null,
	data: null,
};

const INITIAL_SWITCH_ON_OFF_STATE: SwitchOnOffEvent = {
	isServerOn: true,
	disabled: false,
	type: EVENT_TYPE.SWITCH_ON_OFF_EVENT,
	occurrenceTime: null,
	data: null,
};

const INITIAL_SERVER_MAINTENANCE_STATE: ServerMaintenanceEvent = {
	hasError: false,
	hasStarted: false,
	sendNewData: null,
	processDataInServer: null,
	informationInManager: null,
	maintenanceCompleted: null,
	disabled: false,
	type: EVENT_TYPE.SERVER_MAINTENANCE_EVENT,
	occurrenceTime: null,
	data: null,
};

const INITIAL_NETWORK_AGENT_STATE = (data) => {
	return {
		traffic: 0,
		numberOfExecutedJobs: 0,
		numberOfJobsOnHold: 0,
	};
};

const REPORTING_TIME = 1;

export {
	JOB_STATUSES,
	AGENT_TYPES,
	POWER_SHORTAGE_STATE,
	EVENT_TYPE,
	ENERGY_TYPE,
	WELCOMING_MESSAGE,
	ROUTE_TYPES,
	INITIAL_POWER_SHORTAGE_STATE,
	INITIAL_WEATHER_DROP_STATE,
	INITIAL_SWITCH_ON_OFF_STATE,
	INITIAL_SERVER_MAINTENANCE_STATE,
	REPORTING_TIME,
	INITIAL_NETWORK_AGENT_STATE,
};
