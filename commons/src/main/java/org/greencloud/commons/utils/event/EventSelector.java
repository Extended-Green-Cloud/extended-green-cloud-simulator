package org.greencloud.commons.utils.event;

import java.util.List;

import org.greencloud.commons.args.event.EventArgs;
import org.greencloud.commons.enums.event.EventTypeEnum;

/**
 * Class with methods used in selection and filtering collections of events
 */
public class EventSelector {

	/**
	 * Method filters collection of events based on the given type
	 *
	 * @param events        collection of events that is to be filtered
	 * @param eventType     type of the events to select
	 * @param expectedClass class associated with selected type of events
	 * @return events of the selected type
	 */
	public static <T extends EventArgs> List<T> getEventsForType(final List<EventArgs> events,
			final EventTypeEnum eventType, final Class<T> expectedClass) {
		return events.stream()
				.filter(eventArgs -> eventArgs.getType().equals(eventType))
				.map(expectedClass::cast)
				.toList();
	}
}
