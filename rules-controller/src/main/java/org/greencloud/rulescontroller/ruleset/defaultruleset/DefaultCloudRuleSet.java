package org.greencloud.rulescontroller.ruleset.defaultruleset;

import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_CLOUD_RULE_SET;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.df.SearchForSchedulerByClientRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.initial.StartInitialClientBehaviours;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.job.announcing.AnnounceNewJobToSchedulerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.job.listening.ListenForSchedulerJobStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.job.listening.ProcessSchedulerJobStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.adaptation.UpdateRuleSetForWeatherDropRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df.SearchForSchedulerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df.SubscribeServerServiceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df.listening.ListenForServerStatusChangeRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df.listening.ProcessServerStatusChangeCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.listening.ListenForTransferConfirmationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.listening.ListenForTransferRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.listening.ProcessTransferRequestCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.transferring.LookForServerForJobTransferRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.transferring.TransferJobBetweenServersRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.weatherdrop.HandleCNAWeatherDropEventRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.initial.StartInitialCloudNetworkBehaviours;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.announcing.LookForServerForJobExecutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.execution.HandleJobRemovalRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.execution.HandleJobStatusStartCheckRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.execution.ScheduleJobStartVerificationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.ListenForNewScheduledJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.ListenForServerJobStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.ProcessNewScheduledJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.ProcessServerJobStatusUpdateCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.proposing.ProposeToSchedulerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.resource.ListenForServerResourceInformationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.resource.ListenForServerResourceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.resource.processing.ProcessServerResourceInformationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.resource.processing.ProcessServerResourceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.sensor.SenseExternalCloudNetworkEventsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ChangeWeatherPredictionErrorRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ConnectGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.DisconnectGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ProcessConnectGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ProcessDeactivationOfGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ProcessDisconnectingGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ruleset.ListenForServerRuleSetRemovalMessageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ruleset.ListenForServersRuleSetUpdateRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ruleset.ProcessServerRuleSetRemovalMessageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.adaptation.ruleset.ProcessServersRuleSetUpdateRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.dividejob.ProcessGreenSourceJobDivisionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.dividejob.ProcessGreenSourceJobNewInstanceCreationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.dividejob.ProcessGreenSourceJobSubstitutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.ListenForReSupplyRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.ListenForServerErrorInformationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.ProcessReSupplyRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.ProcessServerErrorInformationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.sourcepowershortage.HandleGreenSourcePowerShortageEventRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.sourcepowershortage.ScheduleGreenSourcePowerShortageStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.transfer.TransferInServersRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.transfer.processing.ProcessTransferRefuseCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.weatherdrop.HandleGreenSourceWeatherDropEventRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.weatherdrop.ScheduleGreenSourceWeatherDropFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.weatherdrop.ScheduleGreenSourceWeatherDropStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.initial.StartInitialGreenEnergyBehaviours;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.execution.ProcessManualPowerSupplyFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.execution.ProcessPowerSupplyRemoveRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening.ListenForPowerSupplyStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening.ListenForServerNewJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening.ProcessPowerSupplyStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening.ProcessServerNewJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing.ProcessProposeToServerAcceptResponseRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing.ProposeToServerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.monitor.ReportWeatherPeriodicallyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.sensor.SenseExternalGreenSourceEventsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.weather.RequestWeatherForNewPowerSupplyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.weather.RequestWeatherPeriodicallyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.weather.RequestWeatherToCheckEnergyAfterPowerShortageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.weather.RequestWeatherToVerifyEnergyReSupplyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.weather.SchedulePeriodicWeatherRequestsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.weather.processing.ProcessNotEnoughEnergyForJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.adaptation.IncreaseCPUWeightRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.adaptation.IncreaseDeadlineWeightRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.adaptation.UpdateRuleSetInSchedulerForWeatherDropRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.df.SubscribeCloudNetworkAgentsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.initial.PrepareInitialSchedulerBehavioursRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.AnnounceNewClientJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.LookForCNAForJobExecutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing.ProcessLookForCNAForJobExecutionFailureRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ListenForCNAJobStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ListenForNewClientJobsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ProcessCNAJobStatusUpdateCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ProcessNewClientJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.polling.PollNextClientJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.polling.ProcessPollNextClientJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.priority.ComputeJobPriorityRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.sensor.SenseExternalSchedulerEventsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ChangeGreenSourceWeightRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.DisableServerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.EnableServerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ProcessServerDisablingRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ProcessServerEnablingRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ListenForCNARuleSetRemovalMessageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ListenForRuleSetUpdateRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ProcessCNARuleSetRemovalMessageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ProcessRuleSetUpdateRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.RequestRuleSetUpdateInGreenSourcesRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.SubscribeGreenSourceServiceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.ListenForCNAResourceInformationRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.ListenForGreenSourceServiceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.ProcessGreenSourceServiceUpdateCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing.ProcessCNAResourceInformationRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.dividejob.ProcessJobDivisionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.dividejob.ProcessJobNewInstanceCreationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.dividejob.ProcessJobSubstitutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.errorserver.HandlePowerShortageEventCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.errorserver.SchedulePowerShortageStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.maintenance.ProcessServerMaintenanceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.maintenance.RequestServerMaintenanceInCNARule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply.HandleJobsAffectedByPowerShortageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply.ProcessCheckSingleAffectedJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply.processing.ProcessJobResupplyWithGreenEnergyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ListenForPowerShortageFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ListenForPowerShortageTransferConfirmationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ListenForPowerShortageTransferRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ProcessPowerShortageFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ProcessPowerShortageTransferRequestCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.SchedulePowerShortageJobTransferRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer.TransferInCloudNetworkForGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer.TransferInCloudNetworkRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer.TransferInGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.initial.InitializeResourceKnowledge;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.initial.StartInitialServerBehaviours;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.announcing.LookForGreenSourceForJobExecutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.HandleJobFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.HandleJobStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.ProcessJobFinishOnBackUpPowerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.ProcessJobFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.ProcessJobStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.ListenForUpdatesFromGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.ProcessUpdateFromGreenSourceCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.ListenForJobManualFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.ProcessJobManualFinishCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.ListenForCNANewJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.ProcessCNANewJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.startcheck.ListenForJobStartCheckRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.startcheck.ProcessJobStartCheckRequestCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.price.CalculateServerPriceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.proposing.ProposeInsufficientResourcesRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.proposing.ProposeToCNARule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.sensor.SenseExternalServerEventsRule;

/**
 * Default rule set applied in the system
 */
@SuppressWarnings("unchecked")
public class DefaultCloudRuleSet extends RuleSet {

	public DefaultCloudRuleSet() {
		super(DEFAULT_CLOUD_RULE_SET);
	}

	@Override
	protected List<AgentRule> initializeRules(RulesController<?, ?> rulesController) {
		return new ArrayList<>(switch (rulesController.getAgentProps().getAgentType()) {
			case "SCHEDULER" ->
					getSchedulerRules((RulesController<SchedulerAgentProps, SchedulerNode>) rulesController);
			case "CLIENT" -> getClientRules((RulesController<ClientAgentProps, ClientNode>) rulesController);
			case "CLOUD_NETWORK" ->
					getCNARules((RulesController<CloudNetworkAgentProps, CloudNetworkNode>) rulesController);
			case "SERVER" -> getServerRules((RulesController<ServerAgentProps, ServerNode>) rulesController);
			case "GREEN_ENERGY" ->
					getGreenEnergyRules((RulesController<GreenEnergyAgentProps, GreenEnergyNode>) rulesController);
			default -> new ArrayList<AgentRule>();
		});
	}

	protected List<AgentRule> getClientRules(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		return List.of(
				new SearchForSchedulerByClientRule(rulesController),
				new ListenForSchedulerJobStatusUpdateRule(rulesController, this),
				new StartInitialClientBehaviours(rulesController),
				new AnnounceNewJobToSchedulerRule(rulesController),
				new ProcessSchedulerJobStatusUpdateRule(rulesController)
		);
	}

	protected List<AgentRule> getSchedulerRules(RulesController<SchedulerAgentProps, SchedulerNode> rulesController) {
		return List.of(
				new ProcessLookForCNAForJobExecutionFailureRule(rulesController),
				new PrepareInitialSchedulerBehavioursRule(rulesController),
				new ComputeJobPriorityRule(rulesController),
				new SubscribeCloudNetworkAgentsRule(rulesController),
				new ListenForCNAJobStatusUpdateRule(rulesController, this),
				new ListenForNewClientJobsRule(rulesController, this),
				new PollNextClientJobRule(rulesController),
				new ProcessPollNextClientJobCombinedRule(rulesController, this),
				new AnnounceNewClientJobCombinedRule(rulesController, this),
				new LookForCNAForJobExecutionRule(rulesController),
				new IncreaseCPUWeightRule(rulesController),
				new IncreaseDeadlineWeightRule(rulesController),
				new ProcessCNAJobStatusUpdateCombinedRule(rulesController),
				new ProcessNewClientJobCombinedRule(rulesController),
				new UpdateRuleSetInSchedulerForWeatherDropRule(rulesController),
				new SenseExternalSchedulerEventsRule(rulesController)
		);
	}

	protected List<AgentRule> getCNARules(RulesController<CloudNetworkAgentProps, CloudNetworkNode> rulesController) {
		return List.of(
				new ListenForServerResourceInformationRule(rulesController, this),
				new ProcessServerResourceInformationRule(rulesController),
				new StartInitialCloudNetworkBehaviours(rulesController),
				new LookForServerForJobExecutionRule(rulesController),
				new ProposeToSchedulerRule(rulesController),
				new ListenForServerStatusChangeRule(rulesController, this),
				new ListenForNewScheduledJobRule(rulesController, this),
				new SubscribeServerServiceRule(rulesController),
				new SearchForSchedulerRule(rulesController),
				new ListenForTransferConfirmationRule(rulesController),
				new ListenForTransferRequestRule(rulesController, this),
				new TransferJobBetweenServersRule(rulesController),
				new LookForServerForJobTransferRule(rulesController),
				new ListenForServerJobStatusUpdateRule(rulesController, this),
				new ScheduleJobStartVerificationRule(rulesController),
				new HandleJobStatusStartCheckRule(rulesController),
				new SenseExternalCloudNetworkEventsRule(rulesController),
				new HandleCNAWeatherDropEventRule(rulesController),
				new UpdateRuleSetForWeatherDropRule(rulesController),
				new HandleJobRemovalRule(rulesController),
				new ProcessNewScheduledJobCombinedRule(rulesController),
				new ProcessServerJobStatusUpdateCombinedRule(rulesController),
				new ProcessServerStatusChangeCombinedRule(rulesController),
				new ProcessTransferRequestCombinedRule(rulesController),
				new ListenForServerResourceUpdateRule(rulesController, this),
				new ProcessServerResourceUpdateRule(rulesController)
		);
	}

	protected List<AgentRule> getServerRules(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		return List.of(
				new InitializeResourceKnowledge(rulesController),
				new ListenForCNAResourceInformationRequestRule(rulesController, this),
				new ProcessCNAResourceInformationRequestRule(rulesController),
				new SubscribeGreenSourceServiceRule(rulesController),
				new StartInitialServerBehaviours(rulesController),
				new ListenForGreenSourceServiceUpdateRule(rulesController, this),
				new ProcessServerDisablingRule(rulesController),
				new ProcessServerEnablingRule(rulesController),
				new EnableServerRule(rulesController),
				new DisableServerRule(rulesController),
				new ChangeGreenSourceWeightRule(rulesController),
				new ListenForCNANewJobRule(rulesController, this),
				new LookForGreenSourceForJobExecutionRule(rulesController),
				new CalculateServerPriceRule(rulesController),
				new ProposeInsufficientResourcesRule(rulesController),
				new ProposeToCNARule(rulesController),
				new ProcessJobDivisionRule(rulesController),
				new ProcessJobNewInstanceCreationRule(rulesController),
				new ProcessJobSubstitutionRule(rulesController),
				new HandlePowerShortageEventCombinedRule(rulesController),
				new SchedulePowerShortageStartRule(rulesController),
				new HandleJobsAffectedByPowerShortageRule(rulesController),
				new ProcessJobResupplyWithGreenEnergyRule(rulesController),
				new ListenForPowerShortageFinishRule(rulesController, this),
				new ListenForPowerShortageTransferConfirmationRule(rulesController),
				new ListenForPowerShortageTransferRequestRule(rulesController, this),
				new SchedulePowerShortageJobTransferRule(rulesController),
				new TransferInCloudNetworkForGreenSourceRule(rulesController),
				new TransferInCloudNetworkRule(rulesController),
				new TransferInGreenSourceRule(rulesController),
				new HandleJobFinishRule(rulesController),
				new HandleJobStartRule(rulesController),
				new ProcessJobFinishOnBackUpPowerRule(rulesController),
				new ProcessJobFinishRule(rulesController),
				new ProcessJobStartRule(rulesController),
				new ListenForUpdatesFromGreenSourceRule(rulesController, this),
				new ListenForJobManualFinishRule(rulesController, this),
				new ListenForJobStartCheckRequestRule(rulesController, this),
				new SenseExternalServerEventsRule(rulesController),
				new ListenForRuleSetUpdateRequestRule(rulesController, this),
				new RequestRuleSetUpdateInGreenSourcesRule(rulesController),
				new ListenForCNARuleSetRemovalMessageRule(rulesController, this),
				new ProcessCNANewJobCombinedRule(rulesController),
				new ProcessCNARuleSetRemovalMessageRule(rulesController),
				new ProcessGreenSourceServiceUpdateCombinedRule(rulesController),
				new ProcessJobManualFinishCombinedRule(rulesController),
				new ProcessJobStartCheckRequestCombinedRule(rulesController),
				new ProcessPowerShortageFinishRule(rulesController),
				new ProcessPowerShortageTransferRequestCombinedRule(rulesController),
				new ProcessRuleSetUpdateRequestRule(rulesController),
				new ProcessUpdateFromGreenSourceCombinedRule(rulesController),
				new ProcessCheckSingleAffectedJobRule(rulesController),
				new ProcessServerMaintenanceRule(rulesController),
				new RequestServerMaintenanceInCNARule(rulesController)
		);
	}

	protected List<AgentRule> getGreenEnergyRules(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		return List.of(
				new StartInitialGreenEnergyBehaviours(rulesController),
				new ChangeWeatherPredictionErrorRule(rulesController),
				new ConnectGreenSourceRule(rulesController),
				new DisconnectGreenSourceRule(rulesController),
				new ProcessConnectGreenSourceRule(rulesController),
				new ProcessDeactivationOfGreenSourceRule(rulesController),
				new ProcessDisconnectingGreenSourceRule(rulesController),
				new ProcessGreenSourceJobDivisionRule(rulesController),
				new ProcessGreenSourceJobNewInstanceCreationRule(rulesController),
				new ProcessGreenSourceJobSubstitutionRule(rulesController),
				new ListenForServerErrorInformationRule(rulesController, this),
				new ListenForReSupplyRequestRule(rulesController, this),
				new HandleGreenSourcePowerShortageEventRule(rulesController),
				new ScheduleGreenSourcePowerShortageStartRule(rulesController),
				new TransferInServersRule(rulesController),
				new ProcessManualPowerSupplyFinishRule(rulesController),
				new ProcessPowerSupplyRemoveRule(rulesController),
				new ListenForPowerSupplyStatusUpdateRule(rulesController, this),
				new ListenForServerNewJobRule(rulesController, this),
				new ProcessProposeToServerAcceptResponseRule(rulesController),
				new ProposeToServerRule(rulesController),
				new ReportWeatherPeriodicallyRule(rulesController),
				new SenseExternalGreenSourceEventsRule(rulesController),
				new ProcessNotEnoughEnergyForJobRule(rulesController),
				new RequestWeatherForNewPowerSupplyRule(rulesController),
				new RequestWeatherPeriodicallyRule(rulesController),
				new RequestWeatherToCheckEnergyAfterPowerShortageRule(rulesController),
				new RequestWeatherToVerifyEnergyReSupplyRule(rulesController),
				new SchedulePeriodicWeatherRequestsRule(rulesController),
				new ProcessTransferRefuseCombinedRule(rulesController),
				new HandleGreenSourceWeatherDropEventRule(rulesController),
				new ScheduleGreenSourceWeatherDropStartRule(rulesController),
				new ScheduleGreenSourceWeatherDropFinishRule(rulesController),
				new ListenForServersRuleSetUpdateRequestRule(rulesController, this),
				new ListenForServerRuleSetRemovalMessageRule(rulesController, this),
				new ProcessPowerSupplyStatusUpdateRule(rulesController),
				new ProcessReSupplyRequestRule(rulesController),
				new ProcessServerErrorInformationRule(rulesController),
				new ProcessServerNewJobCombinedRule(rulesController),
				new ProcessServerRuleSetRemovalMessageRule(rulesController),
				new ProcessServersRuleSetUpdateRequestRule(rulesController)
		);
	}
}
