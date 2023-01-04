To run the Green Cloud project there are the following system requirements:
- Java 17
- Maven
- Docker environment
- a shell that has capability to execute bash scripts
For Windows hosts a Git Bash and Docker Desktop combination was tested and is recommended for a streamlined process.
For MacOS hosts ZSH shell and Docker Desktop combination was tested and is recommended for a streamlined process.
Optionally if Python3 is present a browser on http://localhost:3000 with the Green Cloud UI will automatically open.

On first run (or after any source code alterations) execute following command to compile necessary binaries:
    $ mvn clean package

Additionally, after any source code alterations run the following command to remove stale docker images:
	$ ./clean.sh

Usage:
Run application:
	$ ./run.sh
	will run the Green Cloud with default settings.

Run application with parameters:
	$ PARAMS="<runtime parameters>" ./run.sh
	will run the Green Cloud system with provided parameters.

	Available parameter patters:
	1. "run <scenario_name>"
	 	Runs scenario named <scenario_name>
	2. "verify <adaptation_plan> <scenario_name>"
	 	Runs verify with scenario named <scenario_name> for adaptation plan named <adaptation_plan>
	3. "verify+events <adaptation_plan> <scenario_name> <events_scenario_name>"
		Runs verify scenario named <scenario_name> for adaptation plan named <adaptation_plan> with events named
		<events_scenario_name>.

Run application on multiple hosts:
	To run application on multiple hosts one firstly must take note of the following parameters:
	- mainHost - Boolean value denoting if the host is the main one, to which other containers connect.
	- hostId - Host id value denoting which CNA from scenario will run on given host, e.g. for hostId = 1 host will run
	  all agents defined under CNA1, hostId = 0 for non-main host will run the clients generating host.
	- localHostIp - Ip of the main host to which other hosts should connect, default value 127.0.0.1 is used only by
	  the main host.
	- mainHostIp - Host's local IP that is used by the JADE Framework within Docker container for networking with other
	  containers on different hosts. Must be provided as Docker containers by default don't have access to the network
	  outside the Docker container.
	- databaseIp - Local IP of the host running Timescale database. Defaults to IP value used by the Docker, defined by
	  the docker-compose service name.
	- websocketIp - Local IP of the host running socket server. Defaults to IP value used by the Docker, defined by
	  the docker-compose service name.

	Those parameters must be passed to each host when the application is run. To run the multi host environment run
	chosen service at each host. Firstly run timescale, socket-server and frontend services at chosen hosts, by command:
	    $ SERVICE=<service_name> ./run_multi_host_service.sh
	where <service_name> = timescale OR socket-server OR frontend. Make note of local IP addresses of the host where
	you've run the given service. Important!, when running frontend service on a different host than socket-server,
	one must additionally provide local IP of the host running the socket-server, for example:
		$ SERVICE=frontend SOCKET_SERVER_IP=192.168.1.2 ./run_multi_host_service.sh
	Alternatively you can run all services on single host together with the JADE main host using command:
		$ HOST_PARAMS=mainHost=true_hostId=0_localHostIp=<localHostIp>_mainHostIp=127.0.0.1_databaseIp=timescale_websocketIp=socket-server ./run_multi_host.sh
	One can see that host parameters are separated by the underscore character '_'. Eventually one can add scenario
	params which were described above, e.g.:
		$ HOST_PARAMS=<host_params> PARAMS="<runtime parameters>" ./run_multi_host.sh

	With that done one can run main host (if not run together with timescale, socket-server and frontend services) and
	the remote hosts. Use the following command:
		$ SERVICE=backend HOST_PARAMS=<host_params> PARAMS="<runtime parameters>" ./run_multi_host_service.sh
	Example host parameters for remote host (the non-main host):
		HOST_PARAMS=mainHost=false_hostId=1_localHostIp=192.168.1.5_mainHostIp=192.168.1.2_databaseIp=192.168.1.3_websocketIp=192.168.1.4
	will run a host with local ip 192.168.1.5 that will run agents for CNA1, connect to the main host at 192.168.1.2,
	connect to the Timescale database at 192.168.1.3, and to the websocket server at 192.168.1.4.

	Keep in mind that when providing runtime parameters, which override the default values, the same runtime parameters
	must be provided for all the JADE hosts.

Stop application:
	$ ./stop.sh
