# Extended Green Cloud Simulator (EGCS)

Extended Green Cloud Simulator (EGCS) is a self-adaptive multi-agent system used to simulate carbon-intelligent cloud
infrastructures. Specifically, it allows the users to:

- define topologies of cloud networks that are, among others, powered with green-energy sources
- employ and assess different system strategies such as:
    - orchestration of the workload across different cloud network regions
    - job prioritization and scheduling policies
    - handling unexpected environmental events such as dynamic weather changes
- run large-scale distributed simulations including simulations in which agents reside in multiple agent platforms and
  are distributed among different hosts
- experiment with system's autonomic behaviours and the runtime (self-)adaptability

## System architecture and technological requirements

### Technological stack

The system was implemented with the following technologies:

- Java 17 + JADE (Backend)
- EasyRules (Rules Based Expert System) + MVEL (Expression Languages)
- Typescript (Frontend)
- Typescript + Express + Express WS (Socket Server)
- PostgreSQL Timescale (Database)

In order to run the system, it is also required to install:

1. Docker (https://docs.docker.com/get-docker/)
2. Maven (https://maven.apache.org/download.cgi)
3. Shell with the ability to execute bash scripts (e.g. Git BASH for Windows users: https://gitforwindows.org/)
4. Node.js 18+ (https://nodejs.org/en/download)
5. Python 3 (https://www.python.org/downloads/)

### System components

The system consists of the following modules:

- **_agent-connector_** - module containing methods used for generating agent controllers and GUI agent nodes, as well
  as, services (WebSocket-based listener) used to connect the agents with environment to be able to sense external
  events
- **_agent-system_** - module containing definition of EGCS agents
- **_articles_** - module with various showcase agent systems, implemented based on the EGCS agent architecture
- **_commons_** - module containing common methods and classes shared between other modules
- **_data-clustering_** - module containing tools that were used to cluster real-life cloud data and generate the
  synthetic workflows
- **_engine_** - module used to run the simulations. It allows the users to specify the topology of the cloud network,
  pass the desired system strategies and specify test scenario events
- **_green_cloud_ui_** - module containing implementation of GUI
- **_gui_** - module implementing GUI controller with outward WebSockets. It is used to connect JADE agents with the
  frontend
- **_knowledge-database_** - PostgreSQL Timescale database used to store monitoring data for the analysis performed by
  system's managing component
- **_managing-system_** - module realizing agents' adaptations. It implements the architectural MAPE-K model
- **_rules-controller_** - module that defines rules and behaviours that (in the future) will handle
  the system's strategies
- **_socket-server_** - WebSocket module that serves as a proxy between backend and the GUI
- **_weather-api_** - module that contain services responsible for communication with external API used to fetch
  forecasted weather conditions

### External services

The system is using _OpenWeatherMap API_ to retrieve weather predictions: https://openweathermap.org/api.

## Synthetic data generation

In order to generate synthetic data streams please refer to the [Data Stream Generation Instruction](config/STREAM_GENERATION_INSTRUCTION.md).

## System configuration and running

All files used to configure the system are placed in `./config`. The instruction on how to configure the system can be
found in: [Configuration Instruction](config/CONFIG_INSTRUCTION.md).

Similarly, the scripts used to compile the project are in `./compile`. In order to compile and run the project, refer
to: [Compilation Instruction](compile/COMPILE_INSTRUCTION.md).





