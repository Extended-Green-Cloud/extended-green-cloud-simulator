## Configuration instruction of engine module

In contrary to _agent-system_ and _data-clustering_ modules, _engine_ is using multiple configuration files.
The structure of these files is as following:

```
|--/engine
|   |--/examples
|   |
|   |--/knowledge
|   |   |--<initial knowledge file name>.json
|   |
|   |--/properties
|   |   |--scenario.properties
|   |   |--system.properties
|   |
|   |--/samples
|   |   |--<synthetic sample name>.json
|   |
|   |--/scenarios
|   |   |--<scenario name>.json
|   |   |--<scenario events name>.json
|   |
|   |--/strategy
|   |  |--/rulesets
|   |  |--strategy.properties
```

### ./examples

The directory `./examples` contains some old configuration files that can be used by the user as a reference point.
It also contains some example scenarios or events injected using GUI.

### ./knowledge

The directory `./knowledge` contains _.json_ configuration files that can be used to specify (using expression language
MVEL) common ways of handling different resource types in the system.

**_IMPORTANT!_** Currently the specification of initial agents' knowledge is simplified and is a subject of
ongoing work.

Structure of each knowledge file is as follows:

```json lines
{
  "RESOURCE_VALIDATOR": {
    "resource-type-name": <MVEL_expression>,
    ...
  },
  "RESOURCE_COMPARATOR": {
    "resource-type-name_characteristic-name": <MVEL_expression>,
    ...
  },
  "RESOURCE_CHARACTERISTIC_RESERVATION": {
    "resource-type-name_characteristic-name": <MVEL_expression>,
    ...
  },
  "RESOURCE_CHARACTERISTIC_ADDITION": {
    "resource-type-name_characteristic-name": <MVEL_expression>,
    ...
  },
  "RESOURCE_CHARACTERISTIC_SUBTRACTION": {
    "resource-type-name_characteristic-name": <MVEL_expression>,
    ...
  }
}
```

The keys of the main _.json_ object specify the names of the properties used to handle the resources.
Recall, that the current model used to represent the cloud resources is using the following set of resource management
properties:

-   _resourceValidator_ - handler used to assess sufficiency of resources against client demands
-   _resourceComparator_ - handler used to compare values of two resources of the same type
-   _resourceCharacteristicReservation_ - handler used to reserve a given attribute of the resource (e.g. attribute amount
    of CPU) for the client job execution
-   _resourceCharacteristicAddition_ - handler used to specify how two corresponding attributes of resources are added
-   _resourceCharacteristicSubtraction_ - handler used to specify how two corresponding attributes of resources are
    subtracted

Therefore, for example, the object stored under the key _RESOURCE_VALIDATOR_ will specify handlers of different types
of resources that correspond to their _resourceValidator_ property.

To define which handler corresponds to which type of resource/resource characteristic, their names are being used as
keys.
In particular, to configure the handlers use one of the following key naming schemas:

1. `resource-type-name: MVEL expression` - when handler is specified for entire resource, not its individual
   attributes (e.g _resourceValidator_)
2. `resource-type-name_characteristic-name: MVEL expression` - when handler is specified for an individual attribute
   type of selected resource type (e.g. _resourceCharacteristicAddition_).

The logic of the handlers is defined using a single line MVEL expressions (to get familiar with its syntax
check [MVEL documentation]()).
Each type of the handler accepts some input parameters on which the operations can be performed and produces some
output.

#### resourceValidator

Input parameters:

1. **requirements** - object of type Resource which specifies client requirements
2. **resource** - object of type Resource which sufficiency is to be evaluated

Output: [**boolean**] value indicating if the resource is sufficient

#### resourceComparator

Input parameters:

1. **resource1** - first object of type Resource that is to be compared
2. **resource2** - second object of type Resource that is to be compared

Output: [**integer**] 0 if both resources are the same, -1 if this resource is less than another resource and 1
otherwise

#### resourceCharacteristicReservation

Input parameters:

1. **amountToReserve** - object of anonymous type (i.e. can be boolean, integer, double, string etc.) specifying
   amount of given resource that is to be reserved
2. **ownedAmount** - object of anonymous type specifying initially owned amount of resource

Output: [**Object**] amount of resource characteristic after reservation

#### resourceCharacteristicAddition

Input parameters:

1. **resource1** - object of anonymous type in common unit, that specifies amount of first resource that is to be added
2. **resource2** - object of anonymous type in common unit, that specifies amount of second resource that is to be added

Output: [**Object**] amount of resource characteristic after addition

#### resourceCharacteristicSubtraction

Input parameters:

1. **resource1** - object of anonymous type in common unit, that specifies amount of first resource that is to be
   subtracted
2. **resource2** - object of anonymous type in common unit, that specifies amount of second resource that is to be
   subtracted

Output: [**Object**] amount of resource characteristic after subtraction

In order to check the example configuration file, check the `./config/engine/knowledge/exampleInitialKnowledge.json`
file.

### ./properties

The directory `./properties` contains two files (**which names should not be changed**):

-   _scenario.properties_ - file specifying general scenario arguments, including:
    -   `scenario.usesubdirectory` - boolean flag indicating if the scenario file should be taken from a nested
        directory (taken relatively to _/config/engine/scenarios_).
    -   `scenario.subdirectory` - name of the nested directory in which scenario files are placed . While specifying the
        directory path, the user should use `.` instead of separators (e.g. if the scenario file is placed
        in _/scenarios/test/test1_ directory, then path should be given as _test.test1_).
    -   `scenario.generator` - flag indicating method of client tasks generation. Three types of methods are currently
        being handled:
        -   **FROM_SAMPLE** - use predefined tasks mixture sample (note, that sample may be provided by hand or can be
            generated as described in [Data Stream Generation Instruction](config/STREAM_GENERATION_INSTRUCTION.md))
        -   **RANDOM** - generate client tasks with randomized parameters of resource duration (important! note, that
            random generator do not use any knowledge about correlation in consumption between different resource types)
        -   **FROM_EVENTS** - do not pre-generate tasks, but generate them at specific time stamps during the system run
            as specified in the events' configuration file (describe in further sections)
    -   `scenario.structure` - name of the file defining cloud network topology. **IMPORTANT! If
        the `scenario.usesubdirectory` flag is set to true, then the system will look for the network topology file in the
        specified subdirectory!**
    -   `scenario.knowledge` - path to the file containing common initial system knowledge about resource management
        methods
    -   `scenario.events` - name of the file in which events are defined. **IMPORTANT! If the `scenario.usesubdirectory`
        flag is set to true, then the system will look for the event file in the specified subdirectory!**
    -   `scenario.jobssample` - path to the file that contain job sample used in the system run (when generation
        methods **FROM_SAMPLE** is used)
    -   `scenario.clients.number` - specify number of client tasks that are to be generated when the
        **FROM_SAMPLE** or **RANDOM** task generation method is used
    -   `scenario.jobs.*` - specify parameters used to generate random workflows when **RANDOM** tasks generation method
        is used (important! please note that using **RANDOM** and **FROM_SAMPLE** task generation methods allows only to
        generate tasks which define resource demands with respect to _memory_, _storage_ and _cpu_. To define more complex
        client demands, please use **FROM_EVENTS** generation method)
        -   `typesnumber` - number of different task types
        -   `steps.minnumber` - number of minimal number of steps in a task (remember that by default tasks are
            represented as ArgoWorkflows)
        -   `steps.maxnumber` - number of maximal number of steps in a task (set to 1 when the task is to be executed as a
            whole)
        -   `mincpu` - minimum amount of CPU per tasks (in 100 min./CPU core - to read more about duration units
            refer to [ArgoWorkflow resource duration]())
        -   `maxcpu` - maximal amount of CPU per tasks
        -   `minmemory` - minimal amount of memory per task (in 100 min./100Mi)
        -   `maxmemory` - maximal amount of memory per task
        -   `minstorage` - minimal task storage claim (in Gi)
        -   `maxstorage` - maximal task storage claim (in Gi)
        -   `minduration` - shortest task execution duration (in seconds)
        -   `maxduration` - longest task execution duration
        -   `mindeadline` - shortest task execution deadline (in seconds, where 0 indicates that no deadline was
            specified)
        -   `maxdeadline` - longest task execution deadline
-   _system.properties_ - file specifying configuration of the system including parameters of agent platform. If the
    system is to be run on multiple hosts, this file allows to specify how the machines will communicate with each other.
    -   `mtp.*` - specifies default ports used by the JADE message transporter
        -   `intra` - port used for the communication between agents residing in the same platform
        -   `inter` - port used for the communication between agents residing in different platforms
    -   `container.*` - specifies configuration of the agent container and which agents are going to be run
        -   `mainhost` - flag indicating if the container is the main host for **the entire system** (i.e. container
            running _central_ agents such as _Central Manager Agent_ or _Managing Agent_)
        -   `createnew` - flag indicating if the container will run in a new agent platform
        -   `locationId` - corresponds to the value of property field of _Regional Manager Agents_ defined
            in cloud network topology file, which specifies the name of the region, managed by the given _RMA_. By
            passing it in the configuration file, the user outlines that a given container will run only the agents
            that belong to the aforementioned region (e.g. if `locationId` is _RMA1_ it means that only agents that
            are under _RMA_ which manages _RMA1_ region, will be taken under consideration). If the `locationId` is of
            the form _Clients<number>_ then it means that a given container will run only _Client Agents_. If
            the `locationId` is empty, then the system takes under consideration all _RMA_'s agents.
        -   `containerId` - argument similar to `locationId`, but corresponds to the name of the group of _Server Agents_
            that should reside in the same container (it is also specified in the cloud network topology file). If
            `containerId` is left empty, then it means that the container will run only these _Server Agents_, that do not
            have this property specified. **IMPORTANT! If the `createnew` flag is set to true, then `containerId` field is
            being ignored!**
        -   `platformid` - identifier of the platform inside which the container is to be created
        -   `localhostip` - IP address of the host that is running a given container
    -   `main.*` - specifies the configuration of main agent platform of the entire system or the platform in which a
        given container reside.
        -   `hostip` - there are two cases:
            1. If `containerId` is specified - it is the IP address of the host that runs main container in which a give
               sub-container will reside
            2. If `containerId` is not specified - it is the IP address of the host running main container of the entire
               system
        -   `platformid` - it is always the identifier of the platform that runs the main container of entire system
        -   `inter` - there are two cases:
            1. If `containerId` is specified - it is the inter agent communication port of the host that runs main
               container in which a give sub-container will reside
            2. If `containerId` is not specified - it is the inter agent communication port of the host that runs main
               container of the entire system
    -   `service.*` - specifies the configuration of various services running along with the agent system
        -   `database.hostip` - IP address of the host running the database
        -   `websocket.agentsip` - IP address of the host that runs WebSocket responsible for passing agent-related data
        -   `websocket.clientsip` - IP address of the host that runs WebSocket responsible for passing client-related data
        -   `websocket.managingip` - IP address of the host that runs WebSocket responsible for passing adaptation-related
            data
        -   `websocket.networkip` - IP address of the host that runs WebSocket responsible for passing general network
            statistics data
        -   `websocket.eventip` - IP address of the host that runs WebSocket responsible for passing external events to
            agents
    -   `jade.*` - specifies additional configuration responsible for running JADE-related interfaces
        -   `rungui` - flag indicating if the JADE GUI should be run by default along with the system start
        -   `runsniffer` - flag indicating if the JADE Sniffer should be run by default along with the system start

#### Remarks:

1. `container.*` properties do not have to be specified when the system runs on a single machine. They are mostly
   ignored, as the system runs all agents in a single platform.

#### Example configuration:

The following example, will illustrate how to configure a system that is going to run on 4 different hosts:

1. _Host1_ (IP: 10.0.0.0) - running only main system container
2. _Host2_ (IP: 10.0.0.1) - running Regional Manager Agent and its Servers that do not have container specified
3. _Host3_ (IP: 10.0.0.2) - running remaining Servers of Regional Manager Agent that should reside in container of a
   specific name
4. _Host4_ (IP: 10.0.0.3) - running Client Agents

The configuration will focus mainly of the fields of `container.*` and `main.*` as these are the most complicated to
configure.
Let's assume the following network topology:

```
RMA1 (loactionId: Location1):
|-- Server1 (containerId: - )
|   |
|   |-- GreenSource1
|   |   |-- MonitoringAgent1
|   |
|   |-- GreenSource2
|   |   |-- MonitoringAgent2
|
|-- Server2 (containerId: - )
|   |
|   |-- GreenSource3
|   |   |-- MonitoringAgent3
|
|-- Server3 (containerId: Container1)
|   |
|   |-- GreenSource4
|   |   |-- MonitoringAgent4
|
|-- Server4 (containerId: Container1)
|   |
|   |-- GreenSource5
|   |   |-- MonitoringAgent5
|
```

The configuration of the _Host1_ will be as follows:

```
container.mainhost=true

container.createnew=false
container.locationId=Clients0
container.containerId=r

container.platformid=MainPlatform
container.localhostip=10.0.0.0

main.hostip=10.0.0.0
main.platformid=MainPlatform
main.inter=7778
```

This container will run _Central Manager Agent_ and _Managing Agent_.

Now, let us specify the configuration of _Host2_:

```
container.mainhost=false

container.createnew=true
container.locationId=Location1
container.containerId=

container.platformid=RMA1
container.localhostip=10.0.0.1

main.hostip=10.0.0.0
main.platformid=MainPlatform
main.inter=7778
```

The container in _Host2_ will be created in a new agent platform with identifier _RMA1_ and will run _RMA1_, _Server1_
, _Server2_, _GreenSource1_, _GreenSource2_, _GreenSource3_, _Monitoring1_, _Monitoring2_, _Monitoring3_. By using
information of _Host1_ in `main.*` section, the container will be able to communicate with the DF of the main host (
hence, streamlining communication between agents).

Then, let us configure _Host3_:

```
container.mainhost=false

container.createnew=false
container.locationId=Location1
container.containerId=Container1

container.platformid=RMA1
container.localhostip=10.0.0.2

main.hostip=10.0.0.1
main.platformid=MainPlatform
main.inter=7778
```

The container will run _Server3_, _Server4_, _GreenSource3_, _GreenSource4_, _MonitoringAgent3_ and _MonitoringAgent4_.

Finally, the _Host4_ configuration will be as following:

```
container.mainhost=false

container.createnew=true
container.locationId=Clients0
container.containerId=

container.platformid=Clients0
container.localhostip=10.0.0.3

main.hostip=10.0.0.0
main.platformid=MainPlatform
main.inter=7778
```

The last container will run _Client Agents_ in a new agent platform with identifier _Client0_.

### ./samples

Contain files with workflows (i.e. client tasks) samples that are to be used in selected scenarios in the system.
Files from `./samples` are used only when flag **FROM_SAMPLE** is used in the scenario configuration.

Files in the `./samples` directory can be defined:

-   automatically, by being generated using _data-clustering_ module
-   manually, by following sample workflow structure

The workflow model and workflow sample generation method are both described
in [Data Stream Generation Instruction](config/STREAM_GENERATION_INSTRUCTION.md).

### ./scenarios

The directory `./scenarios` contains files that specify network topologies or scenario events. Both of these types of
files are in the _.json_ format. They are passed to the system by indicating their names in configuration files as
described above.

#### Network topology configuration

All agent parameter are defined with _.json_ objects, whereas each type of agent is represented by individual object
type.

##### Managing Agent

-   Key: **"managingAgentArgs"**

```json lines
{
  "name": "name of the agent",
  "systemQualityThreshold": "<required> (double) desired system quality",
  "disableActions": [
    "<optional> name of the adaptation action enum",
    ...
  ]
}
```

##### Central Manager Agent (CMA)

-   Key: **"centralManagerAgentArgs""**

```json lines
{
    "name": "<required> name of the agent",
    "maximumQueueSize": "<required> (int) maximal size of scheduling queue"
}
```

##### Monitoring Agent (MA)

-   _Monitoring Agents_ are defined inside an array under **"monitoringAgentsArgs"** key

```json lines
[
  {
    "name": "name of the agent",
    "badStubProbability": "<optional> probability with which Monitoring Agent can randomly stub bad weather conditions (only works if offline mode is on)"
  },
  ...
]
```

##### Green Energy Agent (GSA)

-   _Green Energy Agents_ are defined inside an array under **"greenEnergyAgentsArgs"** key

```json lines
[
  {
    "name": "<required> name of the agent",
    "monitoringAgent": "<required> local name of connected monitoring agent",
    "ownerSever": "<required> local name of connected server agent",
    "latitude": "<required> (double) latitude component of agent location",
    "longitude": "<required> (double) longitude component of agent location",
    "pricePerPowerUnit": "<required> (double) price per single unit of provided power",
    "maximumCapacity": "<required> (int) maximum capacity of produced energy",
    "energyType": "<required> (enum: WIND/SOLAR) type of energy source",
    "weatherPredictionError": "<required> (double between 0 and 1) error with which weather is predicted"
  },
  ...
]
```

##### Server Agent (SA)

-   _Server Agents_ are defined inside an array under **"serverAgentsArgs"** key

```json lines
{
  "name": "<required> name of the agent",
  "ownerRegionalManager": "<required> local name of parent regional manager agent",
  "jobProcessingLimit": "<required> (int) maximal number of jobs processed at once",
  "price": "<required> (double) price of power unit used in job execution",
  "maxPower": "<required> (int) maximal power consumption",
  "idlePower": "<required> (int) power consumption when server is idle",
  "resources": {
    "cpu": {
      "characteristics": {
        <required>
        "amount": {
          "value": "<required> (double/int) amount of server's CPU",
          "unit": "<optional> unit of CPU amount",
          "toCommonUnitConverter": "<optional> (MVEL or enum value name) converter from current value unit to common one",
          "fromCommonUnitConverter": "<optional> (MVEL or enum value name) converter from common value unit to the one used in particular resource",
          "resourceCharacteristicReservation": "<required> (MVEL or flag TAKE_FROM_INITIAL_KNOWLEDGE) handler of characteristic reservation",
          "resourceCharacteristicSubtraction": "<required> (MVEL or flag TAKE_FROM_INITIAL_KNOWLEDGE) handler of characteristic subtraction",
          "resourceCharacteristicAddition": "<required> (MVEL or flag TAKE_FROM_INITIAL_KNOWLEDGE) handler of characteristic addition"
        },
        "other characteristic name": {
          ...
        }
      },
      "resourceValidator": "<required> (MVEL or flag TAKE_FROM_INITIAL_KNOWLEDGE) handler of resource sufficiency validation",
      "resourceComparator": "<required> (MVEL or flag TAKE_FROM_INITIAL_KNOWLEDGE) handler of resource comparator"
    },
    "other resource name": {
      ...
    }
  },
  "containerId": "<optional> name of the container in which server should reside"
}
```

##### Regional Manager Agent (RMA)

-   _Regional Manager Agents_ are defined inside an array under **"regionalManagerAgentsArgs"** key

```json lines
{
    "name": "<required> name of the agent",
    "locationId": "<optional> name of the region controlled by RMA"
}
```

#### Scenario events configuration

Similarly to agents, events executed in the system are also defined as _.json_ objects.

Currently, the system allows for 9 types of scenario events:

1. _CLIENT_CREATION_EVENT_ - introduces new client to the network
2. _DISABLE_SERVER_EVENT_ - disable selected server network component
3. _ENABLE_SERVER_EVENT_ - enable selected server network component
4. _MODIFY_RULE_SET_ - modify currently applied agent rule set
5. _SERVER_CREATION_EVENT_ - create dynamically new server in the system
6. _GREEN_SOURCE_CREATION_EVENT_ - create dynamically new green source
7. _WEATHER_DROP_EVENT_ - cause fluctuation of weather conditions in selected RMA agents
8. _SERVER_MAINTENANCE_EVENT_ - exchange parts of the servers resources
9. _POWER_SHORTAGE_EVENT_ - cause fluctuation of power in selected network component (SA or GSA)

**Side note:** all of these events can also be generated using the GUI.

##### CLIENT_CREATION_EVENT

-   generated when the value of key **type** is CLIENT_CREATION_EVENT

```json lines
{
  "type": "CLIENT_CREATION_EVENT",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "name": "<required> name of the client that is created",
  "jobId": "<required> unique identifier of client job",
  "job": {
    "duration": "<required> (int) duration in seconds of job execution",
    "deadline": "<required> (int) number of seconds added to duration after which job execution reaches deadline",
    "processorName": "<required> name of type of excuted task",
    "selectionPreference": "<optional> expression in MVEL specifying individualized client preferences",
    "resources": {
      "same object as in SA resource specification"
    },
    "steps": [
      {
        "name": "<required> name of the step",
        "duration": "<required> (int) number of seconds given step is to be executed",
        "requiredResources": {
          "same object as in SA resource specification, but specifying CPU is not required"
        }
      ]
      }
    }
```

Here, _selectionPreference_ is yet another MVEL expression that can be defined.
It accepts 2 parameters:

-   **bestProposal** - currently selected best proposal of job execution. It contains fields (1) _priceForJob_ (i.e. price
    for full job execution at the given component), (2) _typeOfEnergy_ (i.e. type of energy - renewable or not - that is
    to be used for job execution) and (3) _serverResources_ (i.e. resources owned by the server which proposed to carry
    out the job execution).
-   **newProposal** - new proposal that is to be considered. It has the same structure as **bestProposal**

Output of this MVEL expression should be the value 0,-1 or 1, where 0 means that proposals are equivalent, 1 that
**bestProposal** is better and -1 otherwise.

##### DISABLE_SERVER_EVENT

-   generated when the value of key **type** is DISABLE_SERVER_EVENT

```json lines
{
    "type": "DISABLE_SERVER_EVENT",
    "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
    "name": "<required> name of the server that is to be disabled"
}
```

##### ENABLE_SERVER_EVENT

-   generated when the value of key **type** is ENABLE_SERVER_EVENT

```json lines
{
    "type": "ENABLE_SERVER_EVENT",
    "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
    "name": "<required> name of the server that is to be enabled"
}
```

##### MODIFY_RULE_SET

-   generated when the value of key **type** is MODIFY_RULE_SET

```json lines
{
  "type": "MODIFY_RULE_SET",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "agentName": "<required> name of the agent which rules should me modified",
  "fullReplace": "<required> flag indicating if a new rule set is the modification of existing one or something completely new"
  "ruleSetName": "<required> name of the rule set from available rule set that is to be used as replacement"
}
```

**_IMPORTANT!_** Note, that the injected rule set is being passed only by its name.
Therefore, the modification of rule set requires the set to be previously injected into available rule sets.

##### SERVER_CREATION_EVENT

-   generated when the value of key **type** is SERVER_CREATION_EVENT

```json lines
{
  "type": "SERVER_CREATION_EVENT",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "name": "<required> name of the server that is to be created",
  "regionalManager": "<required> name of the RMA to which the server is to be connected",
  "jobProcessingLimit": "<required> (int) maximal number of jobs that can be processed at ones",
  "price": "<required> (double) price per single power unit",
  "maxPower": "<required> (double) maximal capacity of the given server",
  "idlePower": "<required> (double) amount of power produced by the server when it does not execute any job",
  "resources": {
    "same object as in SA resource specification"
  }
}
```

##### GREEN_SOURCE_CREATION_EVENT

-   generated when the value of key **type** is GREEN_SOURCE_CREATION_EVENT

```json lines
{
    "type": "GREEN_SOURCE_CREATION_EVENT",
    "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
    "name": "<required> name of the newly created green energy source",
    "server": "<required> name of the server to which the green source is being connected",
    "latitude": "<required> (int) latitude of the green source's location",
    "longitude": "<required> (int) longitude of the green source's location",
    "pricePerPowerUnit": "<required> (double) price for a single power unit",
    "weatherPredictionError": "<required> (double between 0-1) error taking into account while retrieving weather forecasts ",
    "maximumCapacity": "<required> (double) maximum production capacity of the green source",
    "energyType": "<required> (enum, WIND or SOLAR) type of the energy source"
}
```

#### WEATHER_DROP_EVENT

-   generated when the value of key **type** is WEATHER_DROP_EVENT

```json lines
{
    "type": "WEATHER_DROP_EVENT",
    "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
    "agentName": "<required> name of the RMA agent on which the event is to be executed",
    "duration": "<required> duration of the green source power inaccessibility"
}
```

#### SERVER_MAINTENANCE_EVENT

-   generated when the value of key **type** is SERVER_MAINTENANCE_EVENT

```json lines
{
  "type": "SERVER_MAINTENANCE_EVENT",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "name": "<required> name of the server on which the maintenance is to be conducted",
  "resources": {
    "same object as in SA resource specification"
  }
}
```

#### POWER_SHORTAGE_EVENT

-   generated when the value of key **type** is POWER_SHORTAGE_EVENT

```json lines
{
    "type": "POWER_SHORTAGE_EVENT",
    "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
    "agentName": "<required> name of the component on which the maintenance is to be conducted",
    "isFinished": "<required> (boolean) flag indicating if the event is finishing or starting",
    "cause": "<required> (enum, PHYSICAL_CAUSE or WEATHER_CAUSE) flag indicating the underlying cause of the event"
}
```

The examples of all types of events can be found in `./config/engine/examples/example-events`.

### ./strategy

The directory _strategy_ contains:

1. A single general configuration file _strategy.properties_ file that allows specifying:
    1. the port on which the Spring
       REST Controller will be run (the Spring Controller is being run automatically by the engine in order to allow to
       dynamically inject and modify the rule sets available for the system agents)
    2. name of the default rule set which is to be run at the system start
2. A folder `./rulesets` inside which the rule sets, which are to be automatically injected into the available rule sets
   collection, can be defined

### strategy.properties

The port can be modified by changing the property **api.port**, which by default is set to 5000.
Before changing the port, it should be verified that the port that is to be specified is not being currently used.

The name of the rule set that is to be run initially can be changed using the property **strategy.default**.
If the property is not specified, default rule set called _DEFAULT_RULE_SET_ is to be run.

### rulesets

The rule sets that are to be injected have to be specified in separate _.json_ files.
Each file, specify an entire rule set that is going to, by default, be applied as a modification to the original data
set existing in the application (note: soon it will be possible to define individual data sets, separate from the
default one).

The rule sets must contain following attributes:

-   **name** - unique name of the rule set by which is going to be recognized
-   **rules** - set of rules that the data set comprise.

Currently, there are 13 types of the rules that can be defined:

1. _BASIC_ - standard type of the rule executable by the agents
2. _CHAIN_ - type of rule which after its execution, triggers the repeated evaluation on a current (possibly modified)
   set of facts (i.e. attributes used in the assessment of next rules)
3. _SCHEDULED_ - type of the rule corresponding to the JADE _WakerBehaviour_ handling its consecutive realization steps
4. _PERIODIC_ - type of the rule corresponding to the JADE _TickerBehaciour_ handling its consecutive steps
5. _BEHAVIOUR_ - type of the rule which starts a set of behaviour in the agent (in most cases used to initialize the
   behaviours after agent controller is started)
6. _CFP_ - type of the rule which handles consecutive steps of the JADE _ContractNetInitiator_ template behaviour
7. _LISTENER_ - type of the rule which handles receiving messages of the specific type by the agent
8. _LISTENER_SINGLE_ - corresponds to _MsgReceiver_ JADE template behaviour. Defines handler applied when agent listens
   to a specific singular message
9. _REQUEST_ - type of the rule which handles consecutive steps of the JADE _AchieveREInitiator_ template behaviour
10. _SEARCH_ - type of the rule which is applied when agent looks for the specific agent services in DF
11. _SUBSCRIPTION_ - type of the rule allowing to initiate subscription to the given service and DF and define the
    handlers of received notifications
12. _COMBINED_ - type of the rule which combines multiple rules.

Each type of the rule is defined by its individual properties. It should be noted, that all types extend the _BASIC_
rule type.

#### BASIC agent rule type

The set of following properties are to be specified for each type of the agent rule:

-   **agentRuleType** - one of the 12 available rule types described previously
-   **agentType** - type of the agent for which the rule is to be applied (e.g. RMA or SERVER)
-   **type** - unique name of the rule type. It allows to indicate in which code places, the given rule is going to be
    taken into account
-   **subType** - _(optional)_ works similarly to the **type** property, but is defined only in case of _COMBINED_
    **agentRuleType** to indicate the sub-rules
-   **priority** - _(optional)_ applicable in case of _COMBINED_ **agentRuleType** to describe the order in which the
    rules are to be executed and evaluated
-   **name** - name describing a given rule
-   **description** - description providing more insight on what a given rule is supposed to do
-   **initialParams** - _(optional)_ map of structures that are to be initialized when the rule is created. The main
    purpose of this property, is to allow defining structures, which initialization isn't currently handled by the MVEL
    parser. The map consists of:
    -   _keys_ - names of the initialized structure (names can be used in further MVEL expressions in order to use
        indicated structure)
    -   _values_ - MVELObjectType enums, being one of the following types: _CONCURRENT_MAP_, _MAP_, _LIST_
        and _SET_ (example: `{"emptySet": "SET"}`)
-   **imports** - list of string Java imports that must be specified for any class which functions are called within MVEL
    expression. For example, suppose that in the MVEL expression there is a statement: `Math.ceil(0.98);`. Then, it is
    necessary to add to the list of imports the following: `import java.lang.Math;`.
-   **execute** - MVEL expression (can consist of multiple statements) which is compiled when the rule is being
    executed
-   **evaluate** - MVEL expression (can consist of multiple statements) which is compiled when whether a rule is to be
    executed is evaluated

Note, that the rule of **agentRuleType** _CHAIN_ applies only the set of above properties (i.e., in contrary to the
remaining rule types, it does not contain any additional properties).

#### SCHEDULED agent rule type

The following additional properties should be specified for _SCHEDULED_ **agentRuleType**:

-   **specifyTime** - MVEL expression which calculates the time at which the underlying _WakerBehaviour_ is to be
    executed. It uses a single parameter: facts, which contains a map of attributes of the current partial system
    state (note that the set of facts is implementation-dependent and it is a role of the rules developer to specify which
    attributes it will contain)
-   **handleActionTrigger** - MVEL expression, which is compiled when _WakerBehaviour_ action starts. It also accepts as a
    parameter the set of facts.
-   **evaluateBeforeTrigger** - MVEL expression, which is compiled when the rule execution is evaluated (accepting as
    parameter set of facts).

#### PERIODIC agent rule type

The following additional properties should be specified for _PERIODIC_ **agentRuleType**:

-   **specifyPeriod** - MVEL expression which calculates the period after which the underlying _TickerBehaviour_ action is
    to be executed (accepting facts as the parameter).
-   **handleActionTrigger** - MVEL expression, which is compiled when _TickerBehaviour_ action starts. (accepting as
    parameter set of facts).
-   **evaluateBeforeTrigger** - MVEL expression, which is compiled when the rule execution is evaluated (accepting as
    parameter set of facts).

#### PROPOSAL agent rule type

The following additional properties should be specified for _PROPOSAL_ **agentRuleType**:

-   **createProposalMessage** - MVEL expression which accepts the set of facts as the input parameter and, based on that,
    returns the proposal message that is to be sent.
-   **handleAcceptProposal** - MVEL expression, which is compiled when ACCEPT_PROPOSAL message was received in response (
    accepting as parameter set of facts).
-   **handleRejectProposal** - MVEL expression, which is compiled when REJECT_PROPOSAL message was received in response (
    accepting as parameter set of facts).

#### BEHAVIOUR agent rule type

The following additional properties should be specified for _BEHAVIOUR_ **agentRuleType**:

-   **behaviours** - list of MVEL expression which behaviours are going to be initialized

#### CFP agent rule type

The following additional properties should be specified for _CFP_ **agentRuleType**:

-   **createCFP** - MVEL expression, which accepts the set of facts as the input parameter and, based on that,
    returns the CFP (Call For Proposal) message that is to be sent.
-   **compareProposals** - MVEL expression, which is compiled when new PROPOSAL response is received in order to select
    the best one (accepting as parameter set of facts).
-   **handleRejectProposal** - MVEL expression, which is compiled when REJECT_PROPOSAL message was received in response (
    accepting as parameter set of facts).
-   **handleNoResponses** - MVEL expression, which is compiled when no response messages were received (
    accepting as parameter set of facts).
-   **handleNoProposals** - MVEL expression, which is compiled when all response messages were received and there are no
    proposals (accepting as parameter set of facts).
-   **handleProposals** - MVEL expression, which is compiled when all response messages were received to handle set of the
    proposals (accepting as parameter set of facts). This expression may be particularly usedul, when the best proposal is
    to be selected from the entire response collection, not only by comparing two consecutive responses (as in
    **compareProposals**).

#### LISTENER agent rule type

The following additional properties should be specified for _LISTENER_ **agentRuleType**:

-   **className** - name of the class to which the content of the message can be parsed
-   **messageTemplate** - MVEL expression that creates the template of the message for which the agent will listen to
-   **batchSize** - _(optional)_ number of messages that is to be processed (read) at once in a batch
-   **actionHandler** - MVEL expression compiled when the underlying agent behaviour executed action
-   **selectRuleSetIdx** - _(optional)_ MVEL expression describing the method which is to be used to select the rule set
    index when the messages are to be processed

#### LISTENER_SINGLE agent rule type

The following additional properties should be specified for _LISTENER_SINGLE_ **agentRuleType**:

-   **constructMessageTemplate** - MVEL expression specifying how, based on the set of facts accepted as input, the
    template of the message, for which the agent will listen, is to be created.
-   **specifyExpirationTime** - MVEL expression (accepting facts as input) specifying how the message listening expiration
    time is to be computed (i.e. time in milliseconds indicating the deadline after which the agent will no longer listen
    for a given message).
-   **handleMessageProcessing** - MVEL expression compiled when the message was received and is to be processed (accepting
    facts as input).
-   **handleMessageNotReceived** - MVEL expression compiled when the message was not received (accepting facts as input)

#### REQUEST agent rule type

The following additional properties should be specified for _REQUEST_ **agentRuleType**:

-   **createRequestMessage** - MVEL expression (accepting facts as input) responsible for constructing the REQUEST message
    that is to be sent.
-   **evaluateBeforeForAll** - _(optional)_ MVEL expression that can be used to evaluate if the given rule is applicable
-   **handleInform** - MVEL expression compiled when the INFORM message was received (accepting
    facts as input).
-   **handleInform** - MVEL expression compiled when the FAILURE message was received (accepting
    facts as input).
-   **handleRefuse** - MVEL expression compiled when the REFUSE message was received (accepting
    facts as input).
-   **handleAllResults** - MVEL expression compiled when all responses were received (accepting
    facts as input). It is specifically applicable in case when more than 1 request was sent.

#### SEARCH agent rule type

The following additional properties should be specified for _SEARCH_ **agentRuleType**:

-   **searchAgents** - MVEL expression specifying how to search for the agents (accepting facts as input).
-   **handleNoResults** - MVEL expression compiled when DF returned no responses (accepting facts as input).
-   **handleResults** - MVEL expression compiled when DF returned any response (accepting facts as input).

#### SUBSCRIPTION agent rule type

The following additional properties should be specified for _SUBSCRIPTION_ **agentRuleType**:

-   **createSubscriptionMessage** - MVEL expression (accepting facts as input) that creates a subscription message that is
    to be sent to the DF.
-   **handleRemovedAgents** - MVEL expression compiled when notification message was received indicating that some agent (
    -s) de-registered its (their) service (accepting facts as input).
-   **handleAddedAgents** - MVEL expression compiled when notification message was received indicating that some agent (
    -s) registered its (their) service (accepting facts as input).

#### COMBINED agent rule type

The following additional properties should be specified for _COMBINED_ **agentRuleType**:

-   **combinedRuleType** - type of the rule combination. Currently, two types are supported:
    -   _EXECUTE_FIRST_ - executes only the first sub-rule which passed the facts evaluation
    -   _EXECUTE_ALL_ - executes all sub-rules which passed the facts evaluation
-   **rulesToCombine** - list of rules that are to be combined
