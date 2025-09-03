package org.greencloud.agentsystem.agents.centralmanager;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.DFServiceConstants.CMA_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.CMA_SERVICE_TYPE;
import static org.greencloud.commons.utils.facts.PriorityFactsFactory.constructFactsForPriorityEstimation;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;

import java.util.function.ToDoubleFunction;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.args.agent.centralmanager.factory.CentralManagerArgs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Agent representing the Central Manager that orchestrate the job announcement in system regions.
 */
public class CentralManagerAgent extends EGCSAgent<CMANode, CentralManagerAgentProps, CentralManagerArgs> {

	private static final LogBuilder log = constructLogBuilder(CentralManagerAgent.class);

	/**
	 * Default constructor.
	 */
	public CentralManagerAgent() {
		super();
	}

	@Override
	protected void initializeAgent(final Object[] args) {
		try {
			final EGCSAgentControllerArgs<CentralManagerArgs> controllerArgs = parseAgentArguments(args);
			this.properties = new CentralManagerAgentProps(getName(), controllerArgs.getAgentArgs());

			register(this, getDefaultDF(), CMA_SERVICE_TYPE, CMA_SERVICE_NAME);
		} catch (final Exception e) {
			log.messageError("Incorrect arguments: some parameters for Central Manager Agent are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected void runStartingBehaviours() {
		super.runStartingBehaviours();
		properties.updateGUI();
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), CMA_SERVICE_TYPE, CMA_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected void validateAgentArguments() {
		if (this.properties.getConfig().getMaximumQueueSize() < 1) {
			log.messageError("Incorrect arguments: Queue size must be a positive integer!");
			doDelete();
		}

		if (this.properties.getConfig().getPollingBatchSize() < 1) {
			log.messageError("Incorrect arguments: Polling batch size must be a positive integer!");
			doDelete();
		}
	}

	@Override
	protected void runInitialBehavioursForRuleSet() {
		properties.setUpPriorityQueue(getJobPriority());
		super.runInitialBehavioursForRuleSet();
	}

	private ToDoubleFunction<ClientJob> getJobPriority() {
		return clientJob -> {
			final int index = rulesController.getLatestLongTermRuleSetIdx().get();
			final RuleSetFacts additionalFacts = properties.getPriorityFacts().get(clientJob);
			final RuleSetFacts facts = constructFactsForPriorityEstimation(index, additionalFacts, clientJob);
			fireOnFacts(facts);

			final double result = ofNullable(facts.get(RESULT))
					.map(Double.class::cast)
					.or(() -> ofNullable(clientJob.getPriority()).map(Integer::doubleValue))
					.orElse(0D);

			log.withJobId(clientJob.getJobId())
					.withRuleSetId(index)
					.messageInfo("Priority for job {} was computed and is equal to {}.", clientJob.getJobId(), result);

			properties.getPriorityPerJob().computeIfPresent(clientJob.getJobId(), (k, v) -> result);
			properties.getPriorityPerJob().putIfAbsent(clientJob.getJobId(), result);

			return result;
		};
	}
}
