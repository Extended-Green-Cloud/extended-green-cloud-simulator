package runner.domain;

public final class EngineConstants {

	/**
	 * Boolean value denoting if the host is the main one, to which other containers connect.
	 */
	public static boolean mainHost = true;

	/**
	 * Host id value denoting which CNA from scenario will run on given host, e.g. for hostId = 1 host will run
	 * all agents defined under CNA1.
	 */
	public static int hostId = 0;

	/**
	 * Ip of the main host to which other hosts should connect, default value 127.0.0.1 is used only by the main host.
	 */
	public static String mainHostIp = "127.0.0.1";

	/**
	 * Host's local IP that is used by the JADE Framework within Docker container for networking with other containers
	 * on different hosts. Must be provided as Docker containers by default don't have access to the network outside the
	 * Docker container.
	 */
	public static String localHostIp;

	/**
	 * Local IP of the host running Timescale database. Defaults to IP value used by the Docker, defined by the
	 * docker-compose service name.
	 */
	public static String databaseHostIp = "timescale";

	/**
	 * Local IP of the host running socket server. Defaults to IP value used by the Docker.
	 */
	public static String websocketHostIp = "socket-server";
}
