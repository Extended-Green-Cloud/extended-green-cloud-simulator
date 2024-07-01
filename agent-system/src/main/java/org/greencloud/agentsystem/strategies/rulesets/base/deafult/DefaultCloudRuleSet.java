package org.greencloud.agentsystem.strategies.rulesets.base.deafult;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.allocation.common.validator.regionalmanager.ValidateRegionalServersRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.adaptation.UpdateRuleSetInCMAForWeatherDropRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.df.SubscribeRegionalManagerAgentsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.initial.PrepareInitialCMABehavioursRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.allocation.AllocateNewClientJobsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.allocation.processing.ProcessRegionalManagerDefaultAllocationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.announcing.LookForRMAForJobExecutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.announcing.comparison.CompareProposalsOfJobExecution;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.announcing.processing.ProcessLookForRMAForJobExecutionFailureRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.listening.jobupdate.ListenForRMAJobStatusUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.listening.jobupdate.ProcessRMAJobStatusUpdateCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.listening.newjob.ListenForNewClientJobsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.listening.newjob.ProcessNewClientJobCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.listening.newjob.processing.ProcessNewClientJobAddJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling.PollNextClientJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling.ProcessPollNextClientJobCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling.processing.ProcessNewClientJobTimeAfterDeadlineRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.priority.ComputeJobPriorityRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.priority.PreEvaluateJobPriorityRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.sensor.SenseExternalCMAEventsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.df.SearchForCMAByClientRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.initial.StartInitialClientBehaviours;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.job.announcing.AnnounceNewJobToCMARule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.job.listening.ListenForCMAJobStatusUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.job.listening.ProcessCMAJobStatusUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ChangeWeatherPredictionErrorRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ConnectGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.DisconnectGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ProcessConnectGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ProcessDeactivationOfGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ProcessDisconnectingGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ruleset.ListenForServerRuleSetRemovalMessageRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ruleset.ListenForServersRuleSetUpdateRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ruleset.ProcessServerRuleSetRemovalMessageRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.adaptation.ruleset.ProcessServersRuleSetUpdateRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.dividejob.ProcessGreenSourceJobDivisionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.dividejob.ProcessGreenSourceJobNewInstanceCreationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.dividejob.ProcessGreenSourceJobSubstitutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.servererror.ListenForReSupplyRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.servererror.ListenForServerErrorInformationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.servererror.ProcessReSupplyRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.servererror.ProcessServerErrorInformationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.sourcepowershortage.HandleGreenSourcePowerShortageEventRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.sourcepowershortage.ScheduleGreenSourcePowerShortageStartRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.transfer.TransferInServersRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.transfer.processing.ProcessTransferRefuseCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.weatherdrop.HandleGreenSourceWeatherDropEventRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.weatherdrop.ScheduleGreenSourceWeatherDropFinishRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.events.weatherdrop.ScheduleGreenSourceWeatherDropStartRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.initial.StartInitialGreenEnergyBehaviours;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.execution.ProcessManualPowerSupplyFinishRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.execution.ProcessPowerSupplyRemoveRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.listening.newjob.ListenForServerNewJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.listening.newjob.ProcessServerNewJobCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.listening.supplyupdate.ListenForPowerSupplyStatusUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.listening.supplyupdate.ProcessPowerSupplyStatusUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.proposing.ProcessProposeToServerAcceptResponseRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.proposing.ProposeToServerRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.monitor.ReportWeatherPeriodicallyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.sensor.SenseExternalGreenSourceEventsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather.RequestWeatherForNewPowerSupplyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather.RequestWeatherPeriodicallyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather.RequestWeatherToCheckEnergyAfterPowerShortageRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather.RequestWeatherToVerifyEnergyReSupplyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather.SchedulePeriodicWeatherRequestsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather.processing.ProcessNotEnoughEnergyForJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.adaptation.UpdateRuleSetForWeatherDropRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.df.SearchForCMARule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.df.SubscribeServerServiceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.df.listening.ListenForServerStatusChangeRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.df.listening.ProcessServerStatusChangeCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.errorhandling.weatherdrop.HandleRMAWeatherDropEventRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.errorhandling.weatherdrop.ScheduleWeatherDropAdaptation;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.initial.StartInitialRegionalManagerBehaviours;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.allocation.AllocateServersForNewClientJobsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.allocation.processing.ProcessServerAllocationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.announcing.LookForServerForJobExecutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.announcing.comparison.CompareServersProposalsOfJobExecution;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.execution.HandleJobRemovalRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.execution.HandleJobStatusStartCheckRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.execution.ScheduleJobStartVerificationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobprice.ListenForJobPriceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobprice.processing.ProcessJobPriceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.ListenForServerJobStatusUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.ProcessServerJobStatusUpdateCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.newjob.ListenForNewScheduledJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.newjob.ProcessNewScheduledJobCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.polling.PollNextClientJobForAllocationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.polling.ProcessPollNextClientJobAllocationCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.polling.processing.ProcessPollNextClientJobAfterDeadlineRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.priority.PreEvaluateJobPriorityForRMARule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.proposing.ProposeToCMARule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.proposing.prepare.PrepareProposalForCMA;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.resource.ListenForServerResourceInformationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.resource.ListenForServerResourceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.resource.processing.ProcessServerResourceInformationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.resource.processing.ProcessServerResourceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.sensor.SenseExternalRegionalManagerEventsRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ChangeGreenSourceWeightRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.DisableServerRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.EnableServerRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ProcessServerDisablingRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ProcessServerEnablingRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset.ListenForRMARuleSetRemovalMessageRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset.ListenForRuleSetUpdateRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset.ProcessRMARuleSetRemovalMessageRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset.ProcessRuleSetUpdateRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset.RequestRuleSetUpdateInGreenSourcesRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.df.SubscribeGreenSourceServiceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.df.listening.ListenForGreenSourceServiceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.df.listening.ListenForRMAResourceInformationRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.df.listening.ProcessGreenSourceServiceUpdateCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.df.listening.processing.ProcessRMAResourceInformationRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.dividejob.ProcessJobDivisionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.dividejob.ProcessJobNewInstanceCreationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.dividejob.ProcessJobSubstitutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.errorserver.HandlePowerShortageEventCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.errorserver.schedule.SchedulePowerShortageStartRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.maintenance.RequestServerMaintenanceInRMARule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.maintenance.processing.ProcessServerMaintenanceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.resupply.HandleJobsAffectedByPowerShortageRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.resupply.processing.ProcessCheckSingleAffectedJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.resupply.processing.ProcessJobResupplyWithGreenEnergyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.ListenForPowerShortageFinishRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.ListenForPowerShortageTransferConfirmationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.ListenForPowerShortageTransferRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.ProcessPowerShortageTransferRequestCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.processing.ProcessPowerShortageFinishRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.schedule.SchedulePowerShortageJobTransferRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.transfer.TransferInGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.initial.InitializeResourceKnowledge;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.initial.StartInitialServerBehaviours;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.announcing.LookForGreenSourceForJobExecutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.announcing.comparison.CompareGreenSourceProposalsOfJobExecution;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.execution.HandleJobFinishRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.execution.HandleJobStartRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.execution.processing.ProcessJobFinishExecutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.execution.processing.ProcessJobFinishOnBackUpPowerRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.execution.processing.ProcessJobStartExecutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobprice.HandleJobFinishPriceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobprice.ListenForJobInstancePriceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobprice.processing.ProcessJobInstancePriceUpdateRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate.ListenForUpdatesFromGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate.ProcessUpdateFromGreenSourceCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.manualfinish.ListenForJobManualFinishRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.manualfinish.ProcessJobManualFinishCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.newjob.ListenForRMANewJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.newjob.ProcessRMANewJobCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.startcheck.ListenForJobStartCheckRequestRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.startcheck.ProcessJobStartCheckRequestCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.polling.PollNextClientJobForExecutionRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.polling.ProcessPollNextClientJobForExecutionCombinedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.price.CalculateServerPriceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.proposing.ProposeToRMARule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.proposing.prepare.PrepareProposalForRMA;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.sensor.SenseExternalServerEventsRule;
import org.greencloud.agentsystem.strategies.rulesets.allocation.common.validator.server.ValidateServerErrorRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Default rule set applied in the system
 */
public class DefaultCloudRuleSet extends RuleSet {

	public DefaultCloudRuleSet() {
		super(DEFAULT_RULE_SET, false);
		this.agentRules = new ArrayList<>(initialRules());
	}

	private List<AgentRule> initialRules() {
		return union(gsaRules(),
				union(serverRules(),
						union(rmaRules(),
								union(cmaRules(),
										clientRules())))).stream().toList();
	}

	protected List<AgentRule> clientRules() {
		return List.of(
				new SearchForCMAByClientRule(null),
				new ListenForCMAJobStatusUpdateRule(null, this),
				new StartInitialClientBehaviours(null),
				new AnnounceNewJobToCMARule(null),
				new ProcessCMAJobStatusUpdateRule(null)
		);
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new ProcessLookForRMAForJobExecutionFailureRule(null),
				new PrepareInitialCMABehavioursRule(null),
				new ComputeJobPriorityRule(null),
				new SubscribeRegionalManagerAgentsRule(null),
				new ListenForRMAJobStatusUpdateRule(null, this),
				new ListenForNewClientJobsRule(null, this),
				new PollNextClientJobRule(null),
				new ProcessPollNextClientJobCombinedRule(null, this),
				new ProcessNewClientJobTimeAfterDeadlineRule(null),
				new LookForRMAForJobExecutionRule(null),
				new ProcessRMAJobStatusUpdateCombinedRule(null),
				new ProcessNewClientJobCombinedRule(null),
				new UpdateRuleSetInCMAForWeatherDropRule(null),
				new SenseExternalCMAEventsRule(null),
				new CompareProposalsOfJobExecution(null),
				new PreEvaluateJobPriorityRule(null),
				new ProcessNewClientJobAddJobRule(null),
				new AllocateNewClientJobsRule(null),
				new ProcessRegionalManagerDefaultAllocationRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new ListenForServerResourceInformationRule(null, this),
				new ProcessServerResourceInformationRule(null),
				new StartInitialRegionalManagerBehaviours(null),
				new LookForServerForJobExecutionRule(null),
				new ProposeToCMARule(null),
				new ListenForServerStatusChangeRule(null, this),
				new ListenForNewScheduledJobRule(null, this),
				new SubscribeServerServiceRule(null),
				new SearchForCMARule(null),
				new ListenForServerJobStatusUpdateRule(null, this),
				new ScheduleJobStartVerificationRule(null),
				new HandleJobStatusStartCheckRule(null),
				new SenseExternalRegionalManagerEventsRule(null),
				new HandleRMAWeatherDropEventRule(null),
				new UpdateRuleSetForWeatherDropRule(null),
				new HandleJobRemovalRule(null),
				new ProcessNewScheduledJobCombinedRule(null),
				new ProcessServerJobStatusUpdateCombinedRule(null),
				new ProcessServerStatusChangeCombinedRule(null),
				new ListenForServerResourceUpdateRule(null, this),
				new ProcessServerResourceUpdateRule(null),
				new CompareServersProposalsOfJobExecution(null),
				new ListenForJobPriceUpdateRule(null, this),
				new ProcessJobPriceUpdateRule(null),
				new ScheduleWeatherDropAdaptation(null),
				new PrepareProposalForCMA(null),
				new PollNextClientJobForAllocationRule(null),
				new ProcessPollNextClientJobAllocationCombinedRule(null, this),
				new AllocateServersForNewClientJobsRule(null),
				new ProcessServerAllocationRule(null),
				new ProcessPollNextClientJobAfterDeadlineRule(null),
				new PreEvaluateJobPriorityForRMARule(null),
				new ValidateRegionalServersRule(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new InitializeResourceKnowledge(null),
				new ListenForRMAResourceInformationRequestRule(null, this),
				new ProcessRMAResourceInformationRequestRule(null),
				new SubscribeGreenSourceServiceRule(null),
				new StartInitialServerBehaviours(null),
				new ListenForGreenSourceServiceUpdateRule(null, this),
				new ProcessServerDisablingRule(null),
				new ProcessServerEnablingRule(null),
				new EnableServerRule(null),
				new DisableServerRule(null),
				new ChangeGreenSourceWeightRule(null),
				new ListenForRMANewJobRule(null, this),
				new LookForGreenSourceForJobExecutionRule(null),
				new CalculateServerPriceRule(null),
				new ProposeToRMARule(null),
				new ProcessJobDivisionRule(null),
				new ProcessJobNewInstanceCreationRule(null),
				new ProcessJobSubstitutionRule(null),
				new HandlePowerShortageEventCombinedRule(null),
				new SchedulePowerShortageStartRule(null),
				new HandleJobsAffectedByPowerShortageRule(null),
				new ProcessJobResupplyWithGreenEnergyRule(null),
				new ListenForPowerShortageFinishRule(null, this),
				new ListenForPowerShortageTransferConfirmationRule(null),
				new ListenForPowerShortageTransferRequestRule(null, this),
				new SchedulePowerShortageJobTransferRule(null),
				new TransferInGreenSourceRule(null),
				new HandleJobFinishRule(null),
				new HandleJobStartRule(null),
				new ProcessJobFinishOnBackUpPowerRule(null),
				new ProcessJobFinishExecutionRule(null),
				new ProcessJobStartExecutionRule(null),
				new ListenForJobManualFinishRule(null, this),
				new ListenForJobStartCheckRequestRule(null, this),
				new SenseExternalServerEventsRule(null),
				new ListenForRuleSetUpdateRequestRule(null, this),
				new RequestRuleSetUpdateInGreenSourcesRule(null),
				new ListenForRMARuleSetRemovalMessageRule(null, this),
				new ProcessRMANewJobCombinedRule(null),
				new ProcessRMARuleSetRemovalMessageRule(null),
				new ProcessGreenSourceServiceUpdateCombinedRule(null),
				new ProcessJobManualFinishCombinedRule(null),
				new ProcessJobStartCheckRequestCombinedRule(null),
				new ProcessPowerShortageFinishRule(null),
				new ProcessPowerShortageTransferRequestCombinedRule(null),
				new ProcessRuleSetUpdateRequestRule(null),
				new ProcessCheckSingleAffectedJobRule(null),
				new ProcessServerMaintenanceRule(null),
				new RequestServerMaintenanceInRMARule(null),
				new ListenForJobInstancePriceUpdateRule(null, this),
				new ProcessJobInstancePriceUpdateRule(null),
				new HandleJobFinishPriceUpdateRule(null),
				new CompareGreenSourceProposalsOfJobExecution(null),
				new PollNextClientJobForExecutionRule(null),
				new ProcessPollNextClientJobForExecutionCombinedRule(null, this),
				new PrepareProposalForRMA(null),
				new ListenForUpdatesFromGreenSourceRule(null, this),
				new ProcessUpdateFromGreenSourceCombinedRule(null),
				new ValidateServerErrorRule(null)
		);
	}

	protected List<AgentRule> gsaRules() {
		return List.of(
				new StartInitialGreenEnergyBehaviours(null),
				new ChangeWeatherPredictionErrorRule(null),
				new ConnectGreenSourceRule(null),
				new DisconnectGreenSourceRule(null),
				new ProcessConnectGreenSourceRule(null),
				new ProcessDeactivationOfGreenSourceRule(null),
				new ProcessDisconnectingGreenSourceRule(null),
				new ProcessGreenSourceJobDivisionRule(null),
				new ProcessGreenSourceJobNewInstanceCreationRule(null),
				new ProcessGreenSourceJobSubstitutionRule(null),
				new ListenForServerErrorInformationRule(null, this),
				new ListenForReSupplyRequestRule(null, this),
				new HandleGreenSourcePowerShortageEventRule(null),
				new ScheduleGreenSourcePowerShortageStartRule(null),
				new TransferInServersRule(null),
				new ProcessManualPowerSupplyFinishRule(null),
				new ProcessPowerSupplyRemoveRule(null),
				new ListenForPowerSupplyStatusUpdateRule(null, this),
				new ListenForServerNewJobRule(null, this),
				new ProcessProposeToServerAcceptResponseRule(null),
				new ProposeToServerRule(null),
				new ReportWeatherPeriodicallyRule(null),
				new SenseExternalGreenSourceEventsRule(null),
				new ProcessNotEnoughEnergyForJobRule(null),
				new RequestWeatherForNewPowerSupplyRule(null),
				new RequestWeatherPeriodicallyRule(null),
				new RequestWeatherToCheckEnergyAfterPowerShortageRule(null),
				new RequestWeatherToVerifyEnergyReSupplyRule(null),
				new SchedulePeriodicWeatherRequestsRule(null),
				new ProcessTransferRefuseCombinedRule(null),
				new HandleGreenSourceWeatherDropEventRule(null),
				new ScheduleGreenSourceWeatherDropStartRule(null),
				new ScheduleGreenSourceWeatherDropFinishRule(null),
				new ListenForServersRuleSetUpdateRequestRule(null, this),
				new ListenForServerRuleSetRemovalMessageRule(null, this),
				new ProcessPowerSupplyStatusUpdateRule(null),
				new ProcessReSupplyRequestRule(null),
				new ProcessServerErrorInformationRule(null),
				new ProcessServerNewJobCombinedRule(null),
				new ProcessServerRuleSetRemovalMessageRule(null),
				new ProcessServersRuleSetUpdateRequestRule(null)
		);
	}
}
