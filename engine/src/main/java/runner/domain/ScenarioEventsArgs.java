package runner.domain;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.greencloud.commons.args.event.EventArgs;

/**
 * Arguments of the events triggered in given scenario
 */
public class ScenarioEventsArgs implements Serializable {

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "event")
	private List<EventArgs> eventArgs;

	public ScenarioEventsArgs() {
	}

	/**
	 * Scenario constructor
	 *
	 * @param eventArgs list of events triggered during the scenario execution
	 */
	public ScenarioEventsArgs(List<EventArgs> eventArgs) {
		this.eventArgs = eventArgs;
	}

	public List<EventArgs> getEventArgs() {
		return eventArgs;
	}
}
