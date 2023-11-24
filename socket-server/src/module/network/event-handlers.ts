import { EVENT_TYPE } from "../../constants";
import {
	getEventOccurrenceTime,
	logClientCreationEvent,
	logGreenSourceCreationEvent,
	logServerCreationEvent,
} from "../../utils";

const handleCreateClientEvent = (data) => {
	const { clientData } = data;
	logClientCreationEvent();

	return {
		type: EVENT_TYPE.CLIENT_CREATION_EVENT,
		clientName: clientData.clientName,
		data: {
			...clientData.jobCreator,
			occurrenceTime: getEventOccurrenceTime(0),
		},
	};
};

const handleCreateGreenSourceEvent = (data) => {
	const { greenSourceData } = data;
	logGreenSourceCreationEvent();

	return {
		type: EVENT_TYPE.GREEN_SOURCE_CREATION_EVENT,
		data: {
			...greenSourceData,
			occurrenceTime: getEventOccurrenceTime(0),
		},
	};
};

const handleCreateServerEvent = (data) => {
	const { serverData } = data;
	logServerCreationEvent();

	return {
		type: EVENT_TYPE.SERVER_CREATION_EVENT,
		data: {
			...serverData,
			occurrenceTime: getEventOccurrenceTime(0),
		},
	};
};

export { handleCreateClientEvent, handleCreateGreenSourceEvent, handleCreateServerEvent };
