import { getCurrentTime } from "./time-utils";

export const getEventOccurrenceTime = (time) => {
	const occurrenceTime = new Date(getCurrentTime());
	occurrenceTime.setSeconds(occurrenceTime.getSeconds() + time);
	return occurrenceTime;
};
