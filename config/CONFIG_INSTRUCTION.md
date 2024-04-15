# System configuration

Some system modules require/accept additional configuration parameters that are passed by the user in the dedicated
property files. All of such files are located in this directory (i.e. under `./config`), and are compiled automatically
while running the main building scripts (see [Main README](../README.md)).

Configuration instructions described for each of the modules can be found under the following links:

1. [agent-system module configuration instruction](./AGENT_SYSTEM_CONFIG.md)
2. [data-clustering module configuration instruction](./STREAM_GENERATION_INSTRUCTION.md)
3. [engine module configuration instruction](./ENGINE_CONFIG.md)
4. [green-cloud-ui module configuration instruction](./GREEN_CLOUD_UI_CONFIG.md)
5. [socket-server module configuration instruction](./SOCKET_SERVER_CONFIG.md)
6. [weather-api module configuration instruction](./WEATHER_API_CONFIG.md)

Note, that the default configurations have already been provided, so that it is possible to use them in order to run the
system locally on a single machines. However, the aforementioned configuration options, allow the users to define also
more complex simulations.
