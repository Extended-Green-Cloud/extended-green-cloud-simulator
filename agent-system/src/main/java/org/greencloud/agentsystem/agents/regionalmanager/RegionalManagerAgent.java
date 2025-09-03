package org.greencloud.agentsystem.agents.regionalmanager;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.DFServiceConstants.RMA_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.RMA_SERVICE_TYPE;
import static org.greencloud.commons.utils.facts.PriorityFactsFactory.constructFactsForPriorityEstimation;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.prepareDF;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;

import java.util.List;
import java.util.function.ToDoubleFunction;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing the network component that orchestrates work in part of the Regional Manager.
 */
public class RegionalManagerAgent extends EGCSAgent<RMANode, RegionalManagerAgentProps, RegionalManagerArgs> {

	private static final LogBuilder log = constructLogBuilder(RegionalManagerAgent.class);

	public RegionalManagerAgent() {
		super();
	}

	@Override
	protected void initializeAgent(final Object[] args) {
		try {
			final EGCSAgentControllerArgs<RegionalManagerArgs> controllerArgs = parseAgentArguments(args);
			this.properties = new RegionalManagerAgentProps(getName(), controllerArgs.getAgentArgs());

			properties.setParentDFAddress(prepareDF(controllerArgs.getMainDFAddress().orElseThrow(),
					controllerArgs.getMainHostPlatformId().orElseThrow()));
		} catch (final Exception e) {
			log.messageError("Incorrect arguments: some parameters for RMA are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, properties.getParentDFAddress(), RMA_SERVICE_TYPE, RMA_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		register(this, properties.getParentDFAddress(), RMA_SERVICE_TYPE, RMA_SERVICE_NAME);
		return emptyList();
	}

	@Override
	protected void runInitialBehavioursForRuleSet() {
		properties.setUpPriorityQueue(properties.getConfig().getMaximumQueueSize(), getJobPriority());
		super.runInitialBehavioursForRuleSet();
	}

	private final ToDoubleFunction<ClientJob> getJobPriority() {
		return clientJob -> {
			final int index = rulesController.getLatestLongTermRuleSetIdx().get();
			final RuleSetFacts additionalFacts = properties.getPriorityFacts().get(clientJob);
			final RuleSetFacts facts = constructFactsForPriorityEstimation(index, additionalFacts, clientJob);
			fireOnFacts(facts);

			final double result = ofNullable(facts.get(RESULT)).map(Double.class::cast).orElse(0D);
			log.withJobId(clientJob.getJobId())
					.withRuleSetId(index)
					.messageInfo("Priority for job {} was computed and is equal to {}.", clientJob.getJobId(), result);

			return result;
		};
	}

}
