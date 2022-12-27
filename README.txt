To run the Green Cloud project there are three system requirements:
- Maven
- Docker environment
- a shell that has capability to execute bash scripts
Optionally if Python3 is present a browser on http://localhost:3000 with the Green Cloud UI will automatically open.

On first run (or after any source code alterations) execute following command to compile necessary binaries:
	$ mvn clean package

Additionally, after any source code alterations run command the following command to remove stale docker images:
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
	 	Runs verify with scenario named <scenario_name> for adaptation plan named

	3. "verify+events <adaptation_plan> <scenario_name> <events_scenario_name>"
		Runs verify scenario named <scenario_name> for adaptation plan named <adaptation_plan> with events named <events_scenario_name>.

Stop application:
	$ ./stop.sh
