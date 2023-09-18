package org.greencloud.commons.args.agent;

import java.io.Serializable;

/**
 * Interface with common methods that are implemented in GUI and reused in agent properties
 */
public interface AgentNodeProps<T extends  AgentProps> extends Serializable {

	/**
	 * Method updates GUI of given agent node
	 */
	void updateGUI(final T props);

	/**
	 * Method saves monitoring data of given agent node to database
	 */
	void saveMonitoringData(final T props);
}
