import { EVENT_TYPE } from "../../constants";
import { getEventOccurrenceTime, logClientCreationEvent } from "../../utils";

const handleCreateClientEvent = (data) => {
	const { jobData } = data;
	logClientCreationEvent();

	return {
		type: EVENT_TYPE.CLIENT_CREATION_EVENT,
		data: {
			...jobData,
			occurrenceTime: getEventOccurrenceTime(0),
		},
	};
};

export { handleCreateClientEvent };
