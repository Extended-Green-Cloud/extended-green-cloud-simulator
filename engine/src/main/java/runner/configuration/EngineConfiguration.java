package runner.configuration;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.gui.websocket.enums.SocketTypeEnum.AGENTS_WEB_SOCKET;
import static com.gui.websocket.enums.SocketTypeEnum.CLIENTS_WEB_SOCKET;
import static com.gui.websocket.enums.SocketTypeEnum.EVENTS_WEB_SOCKET;
import static com.gui.websocket.enums.SocketTypeEnum.MANAGING_SYSTEM_WEB_SOCKET;
import static com.gui.websocket.enums.SocketTypeEnum.NETWORK_WEB_SOCKET;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.greencloud.commons.exception.InvalidPropertiesException;
import com.gui.websocket.enums.SocketTypeEnum;

import runner.EngineRunner;

/**
 * Constants used to set up the system (mostly agent platforms)
 * The configuration is by default injected from .properties files
 */
public final class EngineConfiguration extends AbstractConfiguration {

	private static final String PROPERTIES_DIR = "properties";

	/**
	 * Port used for the intra-platform (i.e. inside the platform) agent communication
	 */
	public static String jadeIntraPort;
	/**
	 * Port used for the inter-platform (i.e. between the platforms) agent communication
	 */
	public static String jadeInterPort;
	/**
	 * Boolean value denoting if the container is the main one
	 * (i.e. is the main container of the entire system containing SchedulerAgent and ManagingAgent)
	 */
	public static boolean mainHost;
	/**
	 * Boolean value denoting if the system should start the container in a new JADE platform.
	 */
	public static boolean newPlatform;
	/**
	 * Identifier value denoting which CNA is to be started at the given container.
	 * If the value is null then it starts all CNAs without specified locationId at the same container.
	 * If it is set to "Clients<number>" then it denotes that the container which generates clients is to be started
	 */
	public static String locationId;
	/**
	 * Identifier value denoting which Servers are to be run at the given container
	 * If the identifier is null then it means that it will run the container with CNA and Servers which have no
	 * containerId specified.
	 * The value is treated as null if the newPlatform value is set to true.
	 */
	public static String containerId;
	/**
	 * Name of the platform under which the containers are to be created
	 */
	public static String platformId;
	/**
	 * In case of the sub-container of the platform (i.e. when containerId field is used), it indicates the host
	 * in which the platform's main container is running. Otherwise, it is the address of the host that runs
	 * the main container of entire system.
	 */
	public static String mainHostIp;
	/**
	 * Identifier of the agent platform in which the main container of the entire system runs
	 * This parameter is used to find the relevant DF
	 */
	public static String mainHostPlatformId;
	/**
	 * Port used for the inter-platform (i.e. between the platforms) agent communication of the agent platform
	 * that runs main container of the entire system (or main container with respect to the given sub-container -
	 * when containerId field is used)
	 * This parameter is used to find the relevant DF
	 */
	public static String mainHostInterPort;
	/**
	 * Address of the main Directory Facilitator
	 */
	public static String mainDFAddress;
	/**
	 * Host's local IP that is used by the JADE Framework within Docker container for networking with other containers
	 * on different hosts. Must be provided as Docker containers by default don't have access to the network outside the
	 * Docker container.
	 */
	public static String localHostIp;
	/**
	 * Local IP of the host running Timescale database.
	 */
	public static String databaseHostIp;
	/**
	 * Local IP of the host running socket server processing agents data.
	 */
	public static String agentWebsocketHostIp;
	/**
	 * Local IP of the host running socket server processing clients data.
	 */
	public static String clientWebsocketHostIp;
	/**
	 * Local IP of the host running socket server processing managing system data.
	 */
	public static String managingWebsocketHostIp;
	/**
	 * Local IP of the host running socket server processing cloud network statistics data.
	 */
	public static String networkWebsocketHostIp;
	/**
	 * Local IP of the host running socket server sensing events from gui.
	 */
	public static String eventWebsocketHostIp;
	/**
	 * Addresses of web sockets
	 */
	public static Map<SocketTypeEnum, String> websocketAddresses;
	/**
	 * Flag indicates if the JADE GUI should be started along with the main container
	 */
	public static boolean runJadeGUI;
	/**
	 * Flag indicates if the JADE Sniffer should be started along with the main container
	 */
	public static boolean runJadeSniffer;
	private static String SYSTEM_PROPERTIES_FILE = "system.properties";

	/**
	 * Method sets the system properties file
	 *
	 * @param systemPropertiesFile name of the system properties file
	 */
	public static void setSystemPropertiesFile(final String systemPropertiesFile) {
		SYSTEM_PROPERTIES_FILE = systemPropertiesFile;
	}

	/**
	 * Method reads the properties set for the system set up at the given .properties file
	 */
	public static void readSystemProperties() {
		final Properties props = new Properties();
		try {
			final String pathToSystemProps = buildResourceFilePath(PROPERTIES_DIR, SYSTEM_PROPERTIES_FILE);
			props.load(EngineRunner.class.getClassLoader().getResourceAsStream(pathToSystemProps));

			setUpCommunication(props);
			setUpContainer(props);
			setUpMainHost(props);
			setUpExternalConnection(props);
			setUpJADEGUISettings(props);

		} catch (final IOException | NullPointerException e) {
			throw new InvalidPropertiesException("Could not read properties file:", e);
		}
	}

	private static void setUpCommunication(final Properties props) {
		jadeInterPort = ifNotBlankThenGetOrElse(props.getProperty("mtp.inter"), "7778");
		jadeIntraPort = ifNotBlankThenGetOrElse(props.getProperty("mtp.intra"), "1099");
	}

	private static void setUpContainer(final Properties props) {
		mainHost = parseBoolean(ifNotBlankThenGetOrElse(props.getProperty("container.mainhost"), "true"));
		newPlatform = parseBoolean(ifNotBlankThenGetOrElse(props.getProperty("container.createnew"), "false"));
		locationId = ifNotBlankThenGetOrElse(props.getProperty("container.locationId"), null);
		containerId = newPlatform ? null : ifNotBlankThenGetOrElse(props.getProperty("container.containerId"), null);
		platformId = ifNotBlankThenGetOrElse(props.getProperty("container.platformid"), "MainPlatform");

		if (!isNullOrEmpty(props.getProperty("container.localhostip"))) {
			localHostIp = props.getProperty("container.localhostip");
		}
	}

	private static void setUpMainHost(final Properties props) {
		mainHostIp = ifNotBlankThenGetOrElse(props.getProperty("main.hostip"), "127.0.0.1");
		mainHostPlatformId = ifNotBlankThenGetOrElse(props.getProperty("main.platformid"), "MainPlatform");
		mainHostInterPort = ifNotBlankThenGetOrElse(props.getProperty("main.inter"), "7778");
		mainDFAddress = format("http://%s:%s/acc", mainHostIp, mainHostInterPort);
	}

	private static void setUpExternalConnection(final Properties props) {
		databaseHostIp = ifNotBlankThenGetOrElse(props.getProperty("service.database.hostip"), "localhost");
		agentWebsocketHostIp = ifNotBlankThenGetOrElse(props.getProperty("service.websocket.agentsip"), "localhost");
		clientWebsocketHostIp = ifNotBlankThenGetOrElse(props.getProperty("service.websocket.clientsip"), "localhost");
		managingWebsocketHostIp = ifNotBlankThenGetOrElse(props.getProperty("service.websocket.managingip"),
				"localhost");
		networkWebsocketHostIp = ifNotBlankThenGetOrElse(props.getProperty("service.websocket.networkip"), "localhost");
		eventWebsocketHostIp = ifNotBlankThenGetOrElse(props.getProperty("service.websocket.eventip"), "localhost");
		websocketAddresses = Map.of(
				AGENTS_WEB_SOCKET, format("ws://%s:8080/", agentWebsocketHostIp),
				CLIENTS_WEB_SOCKET, format("ws://%s:8080/", clientWebsocketHostIp),
				MANAGING_SYSTEM_WEB_SOCKET, format("ws://%s:8080/", managingWebsocketHostIp),
				NETWORK_WEB_SOCKET, format("ws://%s:8080/", networkWebsocketHostIp),
				EVENTS_WEB_SOCKET, format("ws://%s:8080/", eventWebsocketHostIp)
		);
	}

	private static void setUpJADEGUISettings(final Properties props) {
		runJadeGUI = parseBoolean(ifNotBlankThenGetOrElse(props.getProperty("jade.rungui"), "true"));
		runJadeSniffer = parseBoolean(ifNotBlankThenGetOrElse(props.getProperty("jade.runsniffer"), "false"));
	}
}
