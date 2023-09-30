package org.greencloud.strategyinjection.agentsystem.agents.restaurant;

import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.register;

import java.util.Map;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.strategyinjection.agentsystem.agents.AbstractAgent;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.greencloud.strategyinjection.agentsystem.domain.CuisineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gui.agents.AgentNode;

/**
 * Agent representing a restaurant
 */
@SuppressWarnings("unchecked")
public class RestaurantAgent extends AbstractAgent<AgentNode<RestaurantAgentProps>, RestaurantAgentProps> {

	private static final Logger logger = LoggerFactory.getLogger(RestaurantAgent.class);

	@Override
	protected void setup() {
		logger.info("Setting up Agent {}", getName());
		final Object[] arguments = getArguments();

		this.rulesController = (RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>>) arguments[0];
		this.properties = new RestaurantAgentProps(getName(), (CuisineType) arguments[1],
				(Map<String, Double>) arguments[2], (Map<String, Object>) arguments[3]);

		register(this, this.getDefaultDF(), "RESTAURANT", "RESTAURANT");
		setRulesController();
	}

	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}
}
