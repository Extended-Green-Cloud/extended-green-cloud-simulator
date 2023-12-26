## Configuration instruction of rules-controller module

Module _rules-controller_ contains:

1. A single general configuration file _application.properties_ file that allows specifying the port on which the Spring
   REST Controller will be run (the Spring Controller is being run automatically by the engine in order to allow to
   dynamically inject and modify the rule sets available for the system agents)
2. A folder `./rulesets` inside which the rule sets, which are to be automatically injected into the available rule sets
   collection, can be defined

### application.properties

The port can be modified by changing the property **server.port**, which by default is set to 5000.
Before changing the port, it should be verified that the port that is to be specified is not being currently used.

### rulesets

The rule sets that are to be injected have to be specified in separate _.json_ files.
Each file, specify an entire rule set that is going to, by default, be applied as a modification to the original data
set existing in the application (note: soon it will be possible to define individual data sets, separate from the
default one).

The rule sets must contain following attributes:

- **name** - unique name of the rule set by which is going to be recognized
- **rules** - set of rules that the data set comprise.

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

- **agentRuleType** - one of the 12 available rule types described previously
- **agentType** - type of the agent for which the rule is to be applied (e.g. RMA or SERVER)
- **type** - unique name of the rule type. It allows to indicate in which code places, the given rule is going to be
  taken into account
- **subType** - _(optional)_ works similarly to the **type** property, but is defined only in case of _COMBINED_
  **agentRuleType** to indicate the sub-rules
- **priority** - _(optional)_ applicable in case of _COMBINED_ **agentRuleType** to describe the order in which the
  rules are to be executed and evaluated
- **name** - name describing a given rule
- **description** - description providing more insight on what a given rule is supposed to do
- **initialParams** - _(optional)_ map of structures that are to be initialized when the rule is created. The main
  purpose of this property, is to allow defining structures, which initialization isn't currently handled by the MVEL
  parser. The map consists of:
    - _keys_ - names of the initialized structure (names can be used in further MVEL expressions in order to use
      indicated structure)
    - _values_ - MVELObjectType enums, being one of the following types: _CONCURRENT_MAP_, _MAP_, _LIST_
      and _SET_ (example: `{"emptySet": "SET"}`)
- **imports** - list of string Java imports that must be specified for any class which functions are called within MVEL
  expression. For example, suppose that in the MVEL expression there is a statement: `Math.ceil(0.98);`. Then, it is
  necessary to add to the list of imports the following: `import java.lang.Math;`.
- **execute** - MVEL expression (can consist of multiple statements) which is compiled when the rule is being
  executed
- **evaluate** - MVEL expression (can consist of multiple statements) which is compiled when whether a rule is to be
  executed is evaluated

Note, that the rule of **agentRuleType** _CHAIN_ applies only the set of above properties (i.e., in contrary to the
remaining rule types, it does not contain any additional properties).

#### SCHEDULED agent rule type

The following additional properties should be specified for _SCHEDULED_ **agentRuleType**:

- **specifyTime** - MVEL expression which calculates the time at which the underlying _WakerBehaviour_ is to be
  executed. It uses a single parameter: facts, which contains a map of attributes of the current partial system
  state (note that the set of facts is implementation-dependent and it is a role of the rules developer to specify which
  attributes it will contain)
- **handleActionTrigger** - MVEL expression, which is compiled when _WakerBehaviour_ action starts. It also accepts as a
  parameter the set of facts.
- **evaluateBeforeTrigger** - MVEL expression, which is compiled when the rule execution is evaluated (accepting as
  parameter set of facts).

#### PERIODIC agent rule type

The following additional properties should be specified for _PERIODIC_ **agentRuleType**:

- **specifyPeriod** - MVEL expression which calculates the period after which the underlying _TickerBehaviour_ action is
  to be executed (accepting facts as the parameter).
- **handleActionTrigger** - MVEL expression, which is compiled when _TickerBehaviour_ action starts. (accepting as
  parameter set of facts).
- **evaluateBeforeTrigger** - MVEL expression, which is compiled when the rule execution is evaluated (accepting as
  parameter set of facts).

#### PROPOSAL agent rule type

The following additional properties should be specified for _PROPOSAL_ **agentRuleType**:

- **createProposalMessage** - MVEL expression which accepts the set of facts as the input parameter and, based on that,
  returns the proposal message that is to be sent.
- **handleAcceptProposal** - MVEL expression, which is compiled when ACCEPT_PROPOSAL message was received in response (
  accepting as parameter set of facts).
- **handleRejectProposal** - MVEL expression, which is compiled when REJECT_PROPOSAL message was received in response (
  accepting as parameter set of facts).

#### BEHAVIOUR agent rule type

The following additional properties should be specified for _BEHAVIOUR_ **agentRuleType**:

- **behaviours** - list of MVEL expression which behaviours are going to be initialized

#### CFP agent rule type

The following additional properties should be specified for _CFP_ **agentRuleType**:

- **createCFP** - MVEL expression, which accepts the set of facts as the input parameter and, based on that,
  returns the CFP (Call For Proposal) message that is to be sent.
- **compareProposals** - MVEL expression, which is compiled when new PROPOSAL response is received in order to select
  the best one (accepting as parameter set of facts).
- **handleRejectProposal** - MVEL expression, which is compiled when REJECT_PROPOSAL message was received in response (
  accepting as parameter set of facts).
- **handleNoResponses** - MVEL expression, which is compiled when no response messages were received (
  accepting as parameter set of facts).
- **handleNoProposals** - MVEL expression, which is compiled when all response messages were received and there are no
  proposals (accepting as parameter set of facts).
- **handleProposals** - MVEL expression, which is compiled when all response messages were received to handle set of the
  proposals (accepting as parameter set of facts). This expression may be particularly usedul, when the best proposal is
  to be selected from the entire response collection, not only by comparing two consecutive responses (as in
  **compareProposals**).

#### LISTENER agent rule type

The following additional properties should be specified for _LISTENER_ **agentRuleType**:

- **className** - name of the class to which the content of the message can be parsed
- **messageTemplate** - MVEL expression that creates the template of the message for which the agent will listen to
- **batchSize** - _(optional)_ number of messages that is to be processed (read) at once in a batch
- **actionHandler** - MVEL expression compiled when the underlying agent behaviour executed action
- **selectRuleSetIdx** - _(optional)_ MVEL expression describing the method which is to be used to select the rule set
  index when the messages are to be processed

#### LISTENER_SINGLE agent rule type

The following additional properties should be specified for _LISTENER_SINGLE_ **agentRuleType**:

- **constructMessageTemplate** - MVEL expression specifying how, based on the set of facts accepted as input, the
  template of the message, for which the agent will listen, is to be created.
- **specifyExpirationTime** - MVEL expression (accepting facts as input) specifying how the message listening expiration
  time is to be computed (i.e. time in milliseconds indicating the deadline after which the agent will no longer listen
  for a given message).
- **handleMessageProcessing** - MVEL expression compiled when the message was received and is to be processed (accepting
  facts as input).
- **handleMessageNotReceived** - MVEL expression compiled when the message was not received (accepting facts as input)

#### REQUEST agent rule type

The following additional properties should be specified for _REQUEST_ **agentRuleType**:

- **createRequestMessage** - MVEL expression (accepting facts as input) responsible for constructing the REQUEST message
  that is to be sent.
- **evaluateBeforeForAll** - _(optional)_ MVEL expression that can be used to evaluate if the given rule is applicable
- **handleInform** - MVEL expression compiled when the INFORM message was received (accepting
  facts as input).
- **handleInform** - MVEL expression compiled when the FAILURE message was received (accepting
  facts as input).
- **handleRefuse** - MVEL expression compiled when the REFUSE message was received (accepting
  facts as input).
- **handleAllResults** - MVEL expression compiled when all responses were received (accepting
  facts as input). It is specifically applicable in case when more than 1 request was sent.

#### SEARCH agent rule type

The following additional properties should be specified for _SEARCH_ **agentRuleType**:

- **searchAgents** - MVEL expression specifying how to search for the agents (accepting facts as input).
- **handleNoResults** - MVEL expression compiled when DF returned no responses (accepting facts as input).
- **handleResults** - MVEL expression compiled when DF returned any response (accepting facts as input).

#### SUBSCRIPTION agent rule type

The following additional properties should be specified for _SUBSCRIPTION_ **agentRuleType**:

- **createSubscriptionMessage** - MVEL expression (accepting facts as input) that creates a subscription message that is
  to be sent to the DF.
- **handleRemovedAgents** - MVEL expression compiled when notification message was received indicating that some agent (
  -s) de-registered its (their) service (accepting facts as input).
- **handleAddedAgents** - MVEL expression compiled when notification message was received indicating that some agent (
  -s) registered its (their) service (accepting facts as input).

#### COMBINED agent rule type

The following additional properties should be specified for _COMBINED_ **agentRuleType**:

- **combinedRuleType** - type of the rule combination. Currently, two types are supported:
  - _EXECUTE_FIRST_ - executes only the first sub-rule which passed the facts evaluation
  - _EXECUTE_ALL_ - executes all sub-rules which passed the facts evaluation
- **rulesToCombine** - list of rules that are to be combined





