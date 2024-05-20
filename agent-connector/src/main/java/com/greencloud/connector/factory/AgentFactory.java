package com.greencloud.connector.factory;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.event.NewClientEventArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.enums.agent.ClientTimeTypeEnum;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.JobCreator;
import org.greencloud.gui.messages.domain.ServerCreator;

/**
 * Interface with a set o methods that create extra agents with specified parameters
 */
public interface AgentFactory {

	/**
	 * Method creates new server agent args that can be used to initialize server agent with default properties.
	 *
	 * @param ownerRMA - required argument specifying owner RMA
	 * @return newly created server agent args
	 */
	ServerArgs createDefaultServerAgent(final String ownerRMA);

	/**
	 * Method creates new server agent args that can be used to initialize server agent.
	 *
	 * @param serverCreator parameters to create server from GUI
	 * @return newly server agent args
	 */
	ServerArgs createServerAgent(final ServerCreator serverCreator);

	/**
	 * Method creates new green energy agent args that can be used to initialize green source agent with default
	 * parameters.
	 *
	 * @param monitoringAgentName required argument specifying monitoring agent name
	 * @param serverName          required argument specifying owner server name
	 * @return newly green energy agent args
	 */
	GreenEnergyArgs createDefaultGreenEnergyAgent(final String monitoringAgentName, final String serverName);

	/**
	 * Method creates new green energy agent args that can be used to initialize green source agent.
	 *
	 * @param greenSourceCreator parameters to create green source from GUI
	 * @param monitoringName     name of monitoring agent to connect with
	 * @return newly green energy agent args
	 */
	GreenEnergyArgs createGreenEnergyAgent(final GreenSourceCreator greenSourceCreator, final String monitoringName);

	/**
	 * Method creates new monitoring agent args that can be used to initialize monitoring agent with default
	 * parameters.
	 *
	 * @return newly created monitoring agent args
	 */
	MonitoringArgs createDefaultMonitoringAgent();

	/**
	 * Method creates new monitoring agent args that can be used to initialize monitoring agent.
	 *
	 * @param name name of agent to be created
	 * @return newly created monitoring agent args
	 */
	MonitoringArgs createMonitoringAgent(final String name);

	/**
	 * Method creates new client agent args that can be used to initialize client agent.
	 *
	 * @param name     client name
	 * @param jobId    job identifier
	 * @param timeType type of time when the client should join cloud (i.e. in simulation time or in real time)
	 * @param jobArgs  specification of the job sent by the client
	 * @return newly created client agent args
	 */
	ClientArgs createClientAgent(final String name, final String jobId, final ClientTimeTypeEnum timeType,
			final JobArgs jobArgs);

	/**
	 * Method creates new client agent args that can be used to initialize client agent.
	 *
	 * @param clientEventArgs arguments to generate new client
	 * @return newly created client agent args
	 */
	ClientArgs createClientAgent(final NewClientEventArgs clientEventArgs);

	/**
	 * Method creates new client agent args from the arguments passed by the GUI.
	 *
	 * @param jobCreator   arguments passed by the GUI
	 * @param clientName   name of the client
	 * @param nextClientId identifier of next client
	 * @return newly created client agent args
	 */
	ClientArgs createClientAgent(final JobCreator jobCreator, final String clientName, final int nextClientId);

}
