import { AGENT_TYPES, POWER_SHORTAGE_STATE, EVENT_TYPE } from "../../constants/index.js";
import { PowerShortageEvent, SwitchOnOffEvent, WeatherDropEvent } from "../../types/agent-event-type.js";
import { getAgentByName, getAgentNodeById, getNodeState } from "../../utils/index.js";
import { logPowerShortageEvent, logSwitchOnOffEvent, logWeatherDropEvent } from "../../utils/logger-utils.js";
import { GRAPH_STATE } from "../graph/graph-state.js";
import { AGENTS_STATE } from "./agents-state.js";

const getEventByType = (events, type) => {
	return events.find((event) => event.type === type);
};

const getEventOccurrenceTime = (time) => {
	const occurrenceTime = new Date();
	occurrenceTime.setSeconds(occurrenceTime.getSeconds() + time);
	return occurrenceTime;
};

async function unlockEvent(event, time) {
	await new Promise((f) => setTimeout(f, time));
	event.disabled = false;
}

const handlePowerShortage = (data) => {
	const agent = getAgentByName(AGENTS_STATE.agents, data.agentName);

	if (agent) {
		const event = getEventByType(agent.events, EVENT_TYPE.POWER_SHORTAGE_EVENT) as PowerShortageEvent;

		if (event) {
			const isEventActive = event.state === POWER_SHORTAGE_STATE.ACTIVE;
			const eventState = isEventActive ? "triggered" : "finished";

			logPowerShortageEvent(agent.name, eventState);

			event.disabled = true;
			const dataToReturn = {
				agentName: agent.name,
				type: EVENT_TYPE.POWER_SHORTAGE_EVENT,
				data: {
					occurrenceTime: getEventOccurrenceTime(2),
					isFinished: event.state !== POWER_SHORTAGE_STATE.ACTIVE,
				},
			};
			event.state = isEventActive ? POWER_SHORTAGE_STATE.INACTIVE : POWER_SHORTAGE_STATE.ACTIVE;

			unlockEvent(event, 3000);
			return dataToReturn;
		}
	}
};

const handleWeatherDrop = (data) => {
	const agent = getAgentByName(AGENTS_STATE.agents, data.agentName);

	if (agent) {
		const event = getEventByType(agent.events, EVENT_TYPE.WEATHER_DROP_EVENT) as WeatherDropEvent;

		if (event) {
			event.disabled = true;

			logWeatherDropEvent(agent.name);

			const dataToReturn = {
				agentName: agent.name,
				type: EVENT_TYPE.WEATHER_DROP_EVENT,
				data: {
					occurrenceTime: getEventOccurrenceTime(5),
					duration: data.data.duration,
				},
			};
			unlockEvent(event, data.data.duration * 1000);
			return dataToReturn;
		}
	}
};

const handleServerSwitchOnOff = (data) => {
	const agent = getAgentByName(AGENTS_STATE.agents, data.agentName);

	if (agent) {
		const event = getEventByType(agent.events, EVENT_TYPE.SWITCH_ON_OFF_EVENT) as SwitchOnOffEvent;

		if (event) {
			event.disabled = true;
			const eventState = event.isServerOn ? "off" : "on";
			const eventType = event.isServerOn ? EVENT_TYPE.SWITCH_OFF_EVENT : EVENT_TYPE.SWITCH_ON_EVENT;

			logSwitchOnOffEvent(agent.name, eventState);

			const dataToReturn = {
				agentName: agent.name,
				type: eventType,
				eventData: {
					occurrenceTime: getEventOccurrenceTime(0),
				},
			};
			return dataToReturn;
		}
	}
};

export { handlePowerShortage, handleWeatherDrop, handleServerSwitchOnOff };
