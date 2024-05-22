package org.greencloud.agentsystem.agents.centralmanager;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.utils.facts.PriorityFactsFactory.constructFactsForPriorityEstimation;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.function.ToDoubleFunction;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

/**
 * Abstract agent class storing the data regarding Central Manager Agent.
 */
public abstract class AbstractCentralManagerAgent extends EGCSAgent<CMANode, CentralManagerAgentProps> {

	private static final Logger logger = getLogger(AbstractCentralManagerAgent.class);

	/**
	 * Default constructor.
	 */
	protected AbstractCentralManagerAgent() {
		super();
		this.properties = new CentralManagerAgentProps(getName());
	}

	/**
	 * Method defines way of calculating the job priority.
	 */
	public final ToDoubleFunction<ClientJob> getJobPriority() {
		return clientJob -> {
			final int index = rulesController.getLatestLongTermRuleSetIdx().get();
			final RuleSetFacts facts =
					constructFactsForPriorityEstimation(index, properties.getPriorityFacts().get(clientJob), clientJob);
			fireOnFacts(facts);

			final double result = ofNullable(facts.get(RESULT)).map(Double.class::cast).orElse(0D);
			logger.info("Priority for job {} was computed and is equal to {}.", clientJob.getJobId(), result);

			return result;
		};
	}
}
