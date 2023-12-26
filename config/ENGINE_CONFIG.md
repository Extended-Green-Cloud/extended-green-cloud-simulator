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
|   |--<scenario name>.json
|   |--<scenario events name>.json
|   |
```

### ./examples

The directory `./examples` contains some old configuration files that can be used by the user as a reference point.
It also contains some example scenarios or events injected using GUI.

### ./knowledge

The directory `./knowledge` contains _.json_ configuration files that can be used to specify (using expression language
MVEL) common ways of handling different resource types in the system.

**_IMPORTANT!_** Currently, feature of specifying the initial agents' knowledge is simplified and is a subject of
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

- _resourceValidator_ - handler used to assess sufficiency of resources against client demands
- _resourceComparator_ - handler used to compare values of two resources of the same type
- _resourceCharacteristicReservation_ - handler used to reserve a given attribute of the resource (e.g. attribute amount
  of CPU) for the client job execution
- _resourceCharacteristicAddition_ - handler used to specify how two corresponding attributes of resources are added
- _resourceCharacteristicSubtraction_ - handler used to specify how two corresponding attributes of resources are
  subtracted

Therefore, for example, the object stored under the key _RESOURCE\_VALIDATOR_ will specify handlers of different types
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

- _scenario.properties_ - file specifying general scenario arguments, including:
    - `scenario.usesubdirectory` - boolean flag indicating if the scenario file should be taken from a nested
      directory (taken relatively to _/config/engine/scenarios_).
    - `scenario.subdirectory` - name of the nested directory in which scenario files are placed . While specifying the
      directory path, the user should use `.` instead of separators (e.g. if the scenario file is placed
      in _/scenarios/test/test1_ directory, then path should be given as _test.test1_).
    - `scenario.generator` - flag indicating method of client tasks generation. Three types of methods are currently
      being handled:
        - **FROM_SAMPLE** - use predefined tasks mixture sample (note, that sample may be provided by hand or can be
          generated as described in [Data Stream Generation Instruction](config/STREAM_GENERATION_INSTRUCTION.md))
        - **RANDOM** - generate client tasks with randomized parameters of resource duration (important! note, that
          random generator do not use any knowledge about correlation in consumption between different resource types)
        - **FROM_EVENTS** - do not pre-generate tasks, but generate them at specific time stamps during the system run
          as specified in the events' configuration file (describe in further sections)
    - `scenario.structure` - name of the file defining cloud network topology. **IMPORTANT! If
      the `scenario.usesubdirectory` flag is set to true, then the system will look for the network topology file in the
      specified subdirectory!**
    - `scenario.knowledge` - path to the file containing common initial system knowledge about resource management
      methods
    - `scenario.events` - name of the file in which events are defined. **IMPORTANT! If the `scenario.usesubdirectory`
      flag is set to true, then the system will look for the event file in the specified subdirectory!**
    - `scenario.jobssample` - path to the file that contain job sample used in the system run (when generation
      methods **FROM_SAMPLE** is used)
    - `scenario.clients.number` - specify number of client tasks that are to be generated when the
      **FROM_SAMPLE** or **RANDOM** task generation method is used
    - `scenario.jobs.*` - specify parameters used to generate random workflows when **RANDOM** tasks generation method
      is used (important! please note that using **RANDOM** and **FROM_SAMPLE** task generation methods allows only to
      generate tasks which define resource demands with respect to _memory_, _storage_ and _cpu_. To define more complex
      client demands, please use **FROM_EVENTS** generation method)
        - `typesnumber` - number of different task types
        - `steps.minnumber` - number of minimal number of steps in a task (remember that by default tasks are
          represented as ArgoWorkflows)
        - `steps.maxnumber` - number of maximal number of steps in a task (set to 1 when the task is to be executed as a
          whole)
        - `mincpu` - minimum amount of CPU per tasks (in 100 min./CPU core - to read more about duration units
          refer to [ArgoWorkflow resource duration]())
        - `maxcpu` - maximal amount of CPU per tasks
        - `minmemory` - minimal amount of memory per task (in 100 min./100Mi)
        - `maxmemory` - maximal amount of memory per task
        - `minstorage` - minimal task storage claim (in Gi)
        - `maxstorage` - maximal task storage claim (in Gi)
        - `minduration` - shortest task execution duration (in seconds)
        - `maxduration` - longest task execution duration
        - `mindeadline` - shortest task execution deadline (in seconds, where 0 indicates that no deadline was
          specified)
        - `maxdeadline` - longest task execution deadline
- _system.properties_ - file specifying configuration of the system including parameters of agent platform. If the
  system is to be run on multiple hosts, this file allows to specify how the machines will communicate with each other.
    - `mtp.*` - specifies default ports used by the JADE message transporter
        - `intra` - port used for the communication between agents residing in the same platform
        - `inter` - port used for the communication between agents residing in different platforms
    - `container.*` - specifies configuration of the agent container and which agents are going to be run
        - `mainhost` - flag indicating if the container is the main host for **the entire system** (i.e. container
          running _central_ agents such as _Scheduler Agent_ or _Managing Agent_)
        - `createnew` - flag indicating if the container will run in a new agent platform
        - `locationId` - corresponds to the value of property field of _Regional Manager Agents_ defined
          in cloud network topology file, which specifies the name of the region, managed by the given _RMA_. By
          passing it in the configuration file, the user outlines that a given container will run only the agents
          that belong to the aforementioned region (e.g. if `locationId` is _RMA1_ it means that only agents that
          are under _RMA_ which manages _RMA1_ region, will be taken under consideration). If the `locationId` is of
          the form _Clients<number>_ then it means that a given container will run only _Client Agents_. If
          the `locationId` is empty, then the system takes under consideration all _RMA_'s agents.
        - `containerId` - argument similar to `locationId`, but corresponds to the name of the group of _Server Agents_
          that should reside in the same container (it is also specified in the cloud network topology file). If
          `containerId` is left empty, then it means that the container will run only these _Server Agents_, that do not
          have this property specified. **IMPORTANT! If the `createnew` flag is set to true, then `containerId` field is
          being ignored!**
        - `platformid` - identifier of the platform inside which the container is to be created
        - `localhostip` - IP address of the host that is running a given container
    - `main.*` - specifies the configuration of main agent platform of the entire system or the platform in which a
      given container reside.
        - `hostip` - there are two cases:
            1. If `containerId` is specified - it is the IP address of the host that runs main container in which a give
               sub-container will reside
            2. If `containerId` is not specified - it is the IP address of the host running main container of the entire
               system
        - `platformid` - it is always the identifier of the platform that runs the main container of entire system
        - `inter` - there are two cases:
            1. If `containerId` is specified - it is the inter agent communication port of the host that runs main
               container in which a give sub-container will reside
            2. If `containerId` is not specified - it is the inter agent communication port of the host that runs main
               container of the entire system
    - `service.*` - specifies the configuration of various services running along with the agent system
        - `database.hostip` - IP address of the host running the database
        - `websocket.agentsip` - IP address of the host that runs WebSocket responsible for passing agent-related data
        - `websocket.clientsip` - IP address of the host that runs WebSocket responsible for passing client-related data
        - `websocket.managingip` - IP address of the host that runs WebSocket responsible for passing adaptation-related
          data
        - `websocket.networkip` - IP address of the host that runs WebSocket responsible for passing general network
          statistics data
        - `websocket.eventip` - IP address of the host that runs WebSocket responsible for passing external events to
          agents
    - `jade.*` - specifies additional configuration responsible for running JADE-related interfaces
        - `rungui` - flag indicating if the JADE GUI should be run by default along with the system start
        - `runsniffer` - flag indicating if the JADE Sniffer should be run by default along with the system start

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

This container will run _Scheduler Agent_ and _Managing Agent_.

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

- automatically, by being generated using _data-clustering_ module
- manually, by following sample workflow structure

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

- Key: **"managingAgentArgs"**

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

##### Scheduler Agent (SCHA)

- Key: **"schedulerAgentArgs""**

```json lines
{
  "name": "<required> name of the agent",
  "deadlineWeight": "<required> (int) priority of job deadline in workload scheduling",
  "cpuWeight": "<required> (int) priority of job CPU demand in workload scheduling",
  "maximumQueueSize": "<required> (int) maximal size of scheduling queue"
}
```

##### Monitoring Agent (MA)

- _Monitoring Agents_ are defined inside an array under **"monitoringAgentsArgs"** key

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

- _Green Energy Agents_ are defined inside an array under **"greenEnergyAgentsArgs"** key

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

- _Server Agents_ are defined inside an array under **"serverAgentsArgs"** key

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

- _Regional Manager Agents_ are defined inside an array under **"regionalManagerAgentsArgs"** key

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

- generated when the value of key **type** is CLIENT_CREATION_EVENT

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

- **bestProposal** - currently selected best proposal of job execution. It contains fields (1) _priceForJob_ (i.e. price
  for full job execution at the given component), (2) _typeOfEnergy_ (i.e. type of energy - renewable or not - that is
  to be used for job execution) and (3) _serverResources_ (i.e. resources owned by the server which proposed to carry
  out the job execution).
- **newProposal** - new proposal that is to be considered. It has the same structure as **bestProposal**

Output of this MVEL expression should be the value 0,-1 or 1, where 0 means that proposals are equivalent, 1 that
**bestProposal** is better and -1 otherwise.

##### DISABLE_SERVER_EVENT

- generated when the value of key **type** is DISABLE_SERVER_EVENT

```json lines
{
  "type": "DISABLE_SERVER_EVENT",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "name": "<required> name of the server that is to be disabled",
}
```

##### ENABLE_SERVER_EVENT

- generated when the value of key **type** is ENABLE_SERVER_EVENT

```json lines
{
  "type": "ENABLE_SERVER_EVENT",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "name": "<required> name of the server that is to be enabled",
}
```

##### MODIFY_RULE_SET

- generated when the value of key **type** is MODIFY_RULE_SET

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

- generated when the value of key **type** is SERVER_CREATION_EVENT

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

- generated when the value of key **type** is GREEN_SOURCE_CREATION_EVENT

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

- generated when the value of key **type** is WEATHER_DROP_EVENT

```json lines
{
  "type": "WEATHER_DROP_EVENT",
  "occurrenceTime": "<required> (int) second from the system start at which the event will be triggered",
  "agentName": "<required> name of the RMA agent on which the event is to be executed",
  "duration": "<required> duration of the green source power inaccessibility"
}
```

#### SERVER_MAINTENANCE_EVENT

- generated when the value of key **type** is SERVER_MAINTENANCE_EVENT

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

- generated when the value of key **type** is POWER_SHORTAGE_EVENT

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