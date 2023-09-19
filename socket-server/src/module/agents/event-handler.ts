import { EVENT_STATE, EVENT_TYPE } from "../../constants/index.js";
import { getAgentByName } from "../../utils/index.js";
import { logPowerShortageEvent, logWeatherDropEvent } from "../../utils/logger-utils.js";
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
		const event = getEventByType(agent.events, EVENT_TYPE.POWER_SHORTAGE_EVENT);

		if (event) {
			const isEventActive = event.state === EVENT_STATE.ACTIVE;
			const eventState = isEventActive ? "triggered" : "finished";
			logPowerShortageEvent(agent.name, eventState);
			event.disabled = true;
			const dataToReturn = {
				agentName: agent.name,
				type: EVENT_TYPE.POWER_SHORTAGE_EVENT,
				data: {
					occurrenceTime: getEventOccurrenceTime(2),
					isFinished: event.state !== EVENT_STATE.ACTIVE,
				},
			};
			event.state = isEventActive ? EVENT_STATE.INACTIVE : EVENT_STATE.ACTIVE;

			unlockEvent(event, 3000);
			return dataToReturn;
		}
	}
};

const handleWeatherDrop = (data) => {
	const agent = getAgentByName(AGENTS_STATE.agents, data.agentName);

	if (agent) {
		const event = getEventByType(agent.events, EVENT_TYPE.WEATHER_DROP_EVENT);

		if (event) {
			event.disabled = true;
			console.log(data);
			logWeatherDropEvent(agent.name);
			const dataToReturn = {
				agentName: agent.name,
				type: EVENT_TYPE.WEATHER_DROP_EVENT,
				data: {
					occurrenceTime: getEventOccurrenceTime(5),
					duration: data.data.duration,
				},
			};
			console.log(dataToReturn);
			unlockEvent(event, data.data.duration * 1000);
			return dataToReturn;
		}
	}
};

export { handlePowerShortage, handleWeatherDrop };
