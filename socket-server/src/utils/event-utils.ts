export const getEventOccurrenceTime = (time) => {
	const occurrenceTime = new Date();
	occurrenceTime.setSeconds(occurrenceTime.getSeconds() + time);
	return occurrenceTime;
};
