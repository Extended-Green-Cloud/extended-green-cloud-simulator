package com.greencloud.connector.factory;

import java.util.Map;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.event.NewClientEventArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.agent.ClientTimeTypeEnum;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.JobCreator;
import org.greencloud.gui.messages.domain.ServerCreator;

/**
 * Interface with a set methods that create extra agents with specified parameters
 */
public interface AgentFactory {

	/**
	 * Method creates new server agent args that can be used to initialize new agent with default maximumCapacity,
	 * price and jobProcessingLimit.
	 *
	 * @param ownerCNA - required argument specifying owner CNA
	 * @return newly created server agent args
	 */
	ServerArgs createDefaultServerAgent(String ownerCNA);

	/**
	 * Method creates new server agent args that can be used to initialize new agent
	 *
	 * @param ownerCNA           - required argument specifying owner CNA
	 * @param resources          - optional argument specifying server's resources
	 * @param maxPower           - optional argument specifying maximal power consumption of the server
	 * @param idlePower          - optional argument specifying idle power consumption of the server
	 * @param price              - optional argument specifying server's price
	 * @param jobProcessingLimit - optional argument specifying maximum number of jobs processed at the same time
	 * @return newly created server agent args
	 */
	ServerArgs createServerAgent(String ownerCNA,
			Map<String, Resource> resources,
			Integer maxPower,
			Integer idlePower,
			Double price,
			Integer jobProcessingLimit);

	/**
	 * Method creates new server agent args that can be used to initialize new agent
	 *
	 * @param serverCreator parameters to create server from GUI
	 *
	 * @return newly server agent args
	 */
	ServerArgs createServerAgent(ServerCreator serverCreator);


	/**
	 * Method creates new green energy agent args that can be used to initialize new agent with default latitude,
	 * longitude, maximumCapacity, pricePerPowerUnit, energyType.
	 *
	 * @param monitoringAgentName required argument specifying monitoring agent name
	 * @param ownerServerName     required argument specifying owner server name
	 * @return newly green energy agent args
	 */
	GreenEnergyArgs createDefaultGreenEnergyAgent(String monitoringAgentName, String ownerServerName);

	/**
	 * Method creates new green energy agent args that can be used to initialize new agent
	 *
	 * @param monitoringAgentName    required argument specifying monitoring agent name
	 * @param ownerServerName        required argument specifying owner server name
	 * @param latitude               optional argument specifying latitude
	 * @param longitude              optional argument specifying longitude
	 * @param maximumCapacity        optional argument specifying maximumCapacity
	 * @param pricePerPowerUnit      optional argument specifying price per power unit
	 * @param weatherPredictionError optional argument specifying weather prediction error
	 * @param energyType             optional argument specifying energy type
	 * @return newly green energy agent args
	 */
	GreenEnergyArgs createGreenEnergyAgent(String monitoringAgentName,
			String ownerServerName,
			Integer latitude,
			Integer longitude,
			Integer maximumCapacity,
			Integer pricePerPowerUnit,
			Double weatherPredictionError,
			GreenEnergySourceTypeEnum energyType);

	/**
	 * Method creates new green energy agent args that can be used to initialize new agent
	 *
	 * @param greenSourceCreator parameters to create green source from GUI
	 * @param monitoringName     name of monitoring agent to connect with
	 * @return newly green energy agent args
	 */
	GreenEnergyArgs createGreenEnergyAgent(GreenSourceCreator greenSourceCreator, final String monitoringName);

	/**
	 * Method creates new monitoring agent args that can be used to initialize new agent
	 *
	 * @return newly created monitoring agent args
	 */
	MonitoringArgs createMonitoringAgent();

	/**
	 * Method creates new monitoring agent args that can be used to initialize new agent
	 *
	 * @param name name of agent to be created
	 * @return newly created monitoring agent args
	 */
	MonitoringArgs createMonitoringAgent(final String name);

	/**
	 * Method creates new client agent args that can be used to initialize new agent
	 *
	 * @param name     client name
	 * @param jobId    job identifier
	 * @param timeType type of time when the client should join cloud (i.e. in simulation time or in real time)
	 * @param jobArgs  specification of the job sent by the client
	 * @return newly created client agent args
	 */
	ClientArgs createClientAgent(String name, String jobId, ClientTimeTypeEnum timeType, JobArgs jobArgs);

	/**
	 * Method creates new client agent args that can be used to initialize new agent
	 *
	 * @param clientEventArgs arguments to generate new client
	 * @return newly created client agent args
	 */
	ClientArgs createClientAgent(NewClientEventArgs clientEventArgs);

	/**
	 * Method creates new client agent args from the arguments passed by the GUI
	 *
	 * @param jobCreator   arguments passed by the GUI
	 * @param clientName name of the client
	 * @param nextClientId identifier of next client
	 * @return newly created client agent args
	 */
	ClientArgs createClientAgent(JobCreator jobCreator, final String clientName, final int nextClientId);

}
