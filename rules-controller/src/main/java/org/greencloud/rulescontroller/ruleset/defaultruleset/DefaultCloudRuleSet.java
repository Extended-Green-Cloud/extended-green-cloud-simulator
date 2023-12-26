package org.greencloud.rulescontroller.ruleset.defaultruleset;

import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_CLOUD_RULE_SET;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
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
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.adaptation.UpdateRuleSetForWeatherDropRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df.SearchForSchedulerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df.SubscribeServerServiceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df.listening.ListenForServerStatusChangeRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df.listening.ProcessServerStatusChangeCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.listening.ListenForTransferConfirmationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.listening.ListenForTransferRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.listening.ProcessTransferRequestCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.transferring.LookForServerForJobTransferRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.transferring.TransferJobBetweenServersRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.weatherdrop.HandleRMAWeatherDropEventRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.errorhandling.weatherdrop.ScheduleWeatherDropAdaptation;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.initial.StartInitialRegionalManagerBehaviours;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.announcing.LookForServerForJobExecutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.announcing.comparison.CompareServersProposalsOfJobExecution;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.execution.HandleJobRemovalRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.execution.HandleJobStatusStartCheckRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.execution.ScheduleJobStartVerificationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.ListenForJobPriceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.ListenForNewScheduledJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.ListenForServerJobStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.ProcessNewScheduledJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.ProcessServerJobStatusUpdateCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.listening.processing.ProcessJobPriceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.job.proposing.ProposeToSchedulerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.resource.ListenForServerResourceInformationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.resource.ListenForServerResourceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.resource.processing.ProcessServerResourceInformationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.resource.processing.ProcessServerResourceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.sensor.SenseExternalRegionalManagerEventsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.adaptation.IncreaseCPUWeightRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.adaptation.IncreaseDeadlineWeightRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.adaptation.UpdateRuleSetInSchedulerForWeatherDropRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.df.SubscribeRegionalManagerAgentsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.initial.PrepareInitialSchedulerBehavioursRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.AnnounceNewClientJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.LookForRMAForJobExecutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.comparison.CompareProposalsOfJobExecution;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing.ProcessLookForRMAForJobExecutionFailureRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ListenForNewClientJobsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ListenForRMAJobStatusUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ProcessNewClientJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.ProcessRMAJobStatusUpdateCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.polling.PollNextClientJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.polling.ProcessPollNextClientJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.priority.ComputeJobPriorityRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.sensor.SenseExternalSchedulerEventsRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ChangeGreenSourceWeightRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.DisableServerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.EnableServerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ProcessServerDisablingRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ProcessServerEnablingRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ListenForRMARuleSetRemovalMessageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ListenForRuleSetUpdateRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ProcessRMARuleSetRemovalMessageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.ProcessRuleSetUpdateRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset.RequestRuleSetUpdateInGreenSourcesRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.SubscribeGreenSourceServiceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.ListenForGreenSourceServiceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.ListenForRMAResourceInformationRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.ProcessGreenSourceServiceUpdateCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing.ProcessRMAResourceInformationRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.dividejob.ProcessJobDivisionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.dividejob.ProcessJobNewInstanceCreationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.dividejob.ProcessJobSubstitutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.errorserver.HandlePowerShortageEventCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.errorserver.SchedulePowerShortageStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.maintenance.ProcessServerMaintenanceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.maintenance.RequestServerMaintenanceInRMARule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply.HandleJobsAffectedByPowerShortageRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply.ProcessCheckSingleAffectedJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply.processing.ProcessJobResupplyWithGreenEnergyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ListenForPowerShortageFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ListenForPowerShortageTransferConfirmationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ListenForPowerShortageTransferRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ProcessPowerShortageFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.ProcessPowerShortageTransferRequestCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.SchedulePowerShortageJobTransferRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer.TransferInRegionalManagerForGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer.TransferInRegionalManagerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer.TransferInGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.initial.InitializeResourceKnowledge;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.initial.StartInitialServerBehaviours;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.announcing.LookForGreenSourceForJobExecutionRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.HandleJobFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.HandleJobStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.ProcessJobFinishOnBackUpPowerRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.ProcessJobFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution.ProcessJobStartRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobprice.HandleJobFinishPriceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobprice.ListenForJobInstancePriceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobprice.processing.ProcessJobInstancePriceUpdateRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.ListenForUpdatesFromGreenSourceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.ProcessUpdateFromGreenSourceCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.ListenForJobManualFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.ProcessJobManualFinishCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.ListenForRMANewJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.newjob.ProcessRMANewJobCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.startcheck.ListenForJobStartCheckRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.startcheck.ProcessJobStartCheckRequestCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.price.CalculateServerPriceRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.proposing.ProposeInsufficientResourcesRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.proposing.ProposeToRMARule;
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
			case "REGIONAL_MANAGER" ->
					getRMARules((RulesController<RegionalManagerAgentProps, RegionalManagerNode>) rulesController);
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
				new ProcessLookForRMAForJobExecutionFailureRule(rulesController),
				new PrepareInitialSchedulerBehavioursRule(rulesController),
				new ComputeJobPriorityRule(rulesController),
				new SubscribeRegionalManagerAgentsRule(rulesController),
				new ListenForRMAJobStatusUpdateRule(rulesController, this),
				new ListenForNewClientJobsRule(rulesController, this),
				new PollNextClientJobRule(rulesController),
				new ProcessPollNextClientJobCombinedRule(rulesController, this),
				new AnnounceNewClientJobCombinedRule(rulesController, this),
				new LookForRMAForJobExecutionRule(rulesController),
				new IncreaseCPUWeightRule(rulesController),
				new IncreaseDeadlineWeightRule(rulesController),
				new ProcessRMAJobStatusUpdateCombinedRule(rulesController),
				new ProcessNewClientJobCombinedRule(rulesController),
				new UpdateRuleSetInSchedulerForWeatherDropRule(rulesController),
				new SenseExternalSchedulerEventsRule(rulesController),
				new CompareProposalsOfJobExecution(rulesController)
		);
	}

	protected List<AgentRule> getRMARules(
			RulesController<RegionalManagerAgentProps, RegionalManagerNode> rulesController) {
		return List.of(
				new ListenForServerResourceInformationRule(rulesController, this),
				new ProcessServerResourceInformationRule(rulesController),
				new StartInitialRegionalManagerBehaviours(rulesController),
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
				new SenseExternalRegionalManagerEventsRule(rulesController),
				new HandleRMAWeatherDropEventRule(rulesController),
				new UpdateRuleSetForWeatherDropRule(rulesController),
				new HandleJobRemovalRule(rulesController),
				new ProcessNewScheduledJobCombinedRule(rulesController),
				new ProcessServerJobStatusUpdateCombinedRule(rulesController),
				new ProcessServerStatusChangeCombinedRule(rulesController),
				new ProcessTransferRequestCombinedRule(rulesController),
				new ListenForServerResourceUpdateRule(rulesController, this),
				new ProcessServerResourceUpdateRule(rulesController),
				new CompareServersProposalsOfJobExecution(rulesController),
				new ListenForJobPriceUpdateRule(rulesController, this),
				new ProcessJobPriceUpdateRule(rulesController),
				new ScheduleWeatherDropAdaptation(rulesController)
		);
	}

	protected List<AgentRule> getServerRules(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		return List.of(
				new InitializeResourceKnowledge(rulesController),
				new ListenForRMAResourceInformationRequestRule(rulesController, this),
				new ProcessRMAResourceInformationRequestRule(rulesController),
				new SubscribeGreenSourceServiceRule(rulesController),
				new StartInitialServerBehaviours(rulesController),
				new ListenForGreenSourceServiceUpdateRule(rulesController, this),
				new ProcessServerDisablingRule(rulesController),
				new ProcessServerEnablingRule(rulesController),
				new EnableServerRule(rulesController),
				new DisableServerRule(rulesController),
				new ChangeGreenSourceWeightRule(rulesController),
				new ListenForRMANewJobRule(rulesController, this),
				new LookForGreenSourceForJobExecutionRule(rulesController),
				new CalculateServerPriceRule(rulesController),
				new ProposeInsufficientResourcesRule(rulesController),
				new ProposeToRMARule(rulesController),
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
				new TransferInRegionalManagerForGreenSourceRule(rulesController),
				new TransferInRegionalManagerRule(rulesController),
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
				new ListenForRMARuleSetRemovalMessageRule(rulesController, this),
				new ProcessRMANewJobCombinedRule(rulesController),
				new ProcessRMARuleSetRemovalMessageRule(rulesController),
				new ProcessGreenSourceServiceUpdateCombinedRule(rulesController),
				new ProcessJobManualFinishCombinedRule(rulesController),
				new ProcessJobStartCheckRequestCombinedRule(rulesController),
				new ProcessPowerShortageFinishRule(rulesController),
				new ProcessPowerShortageTransferRequestCombinedRule(rulesController),
				new ProcessRuleSetUpdateRequestRule(rulesController),
				new ProcessUpdateFromGreenSourceCombinedRule(rulesController),
				new ProcessCheckSingleAffectedJobRule(rulesController),
				new ProcessServerMaintenanceRule(rulesController),
				new RequestServerMaintenanceInRMARule(rulesController),
				new ListenForJobInstancePriceUpdateRule(rulesController, this),
				new ProcessJobInstancePriceUpdateRule(rulesController),
				new HandleJobFinishPriceUpdateRule(rulesController)
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
