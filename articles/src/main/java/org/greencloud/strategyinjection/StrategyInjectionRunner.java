package org.greencloud.strategyinjection;

import static jade.core.Runtime.instance;
import static org.greencloud.rulescontroller.rest.RuleSetRestApi.startRulesControllerRest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.ImmutableAgentArgs;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.args.ImmutableRestaurantAgentArgs;
import org.greencloud.strategyinjection.agentsystem.agents.restaurant.args.RestaurantAgentArgs;
import org.greencloud.strategyinjection.agentsystem.domain.CuisineType;
import org.greencloud.strategyinjection.agentsystem.ruleset.DefaultRestaurantRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.factory.AgentControllerFactory;
import com.greencloud.factory.AgentControllerFactoryImpl;
import com.gui.websocket.GuiWebSocketServer;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Runner of the rule set injection testing system
 */
public class StrategyInjectionRunner {

	public static final String RESTAURANT = "org.greencloud.strategyinjection.agentsystem.agents.restaurant.RestaurantAgent";
	public static final String BOOKING = "org.greencloud.strategyinjection.agentsystem.agents.booking.BookingAgent";
	private static final Logger logger = LoggerFactory.getLogger(StrategyInjectionRunner.class);

	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	public static void main(String[] args)
			throws ExecutionException, InterruptedException, StaleProxyException {
		logger.info("Running example system that performs tests of rule set injection algorithms.");

		final ContainerController jadeController = runMainContainer();
		final AgentControllerFactory factory = new AgentControllerFactoryImpl(jadeController);
		final GuiWebSocketServer server = new GuiWebSocketServer();
		startRulesControllerRest(new DefaultRestaurantRuleSet());
		server.start();

		final List<RestaurantAgentArgs> restaurants = getRestaurants();
		restaurants.forEach(restaurant -> {
			var agentController = factory.createAgentController(restaurant, RESTAURANT);
			factory.runAgentController(agentController, 5);
		});

		var agentController = factory.createAgentController(getBookingAgent(), BOOKING);
		factory.runAgentController(agentController, 0);
	}

	private static AgentArgs getBookingAgent() {
		return ImmutableAgentArgs.builder().name("BookingAgent").build();
	}

	private static List<RestaurantAgentArgs> getRestaurants() {
		return List.of(
				ImmutableRestaurantAgentArgs.builder()
						.name("ItalianRestaurant1")
						.cuisineType(CuisineType.ITALIAN)
						.dishWithPrice(Map.of("Pasta", 50.0, "Pizza", 100.5))
						.build(),
				ImmutableRestaurantAgentArgs.builder()
						.name("ItalianRestaurant2")
						.cuisineType(CuisineType.ITALIAN)
						.dishWithPrice(Map.of("Pasta", 100.0, "Pizza", 100.5))
						.additionalInformation(Map.of(
								"smoking", false,
								"dog_friendly", true
						))
						.build()
		);
	}

	private static ContainerController runMainContainer()
			throws ExecutionException, InterruptedException, StaleProxyException {
		final Runtime jadeRuntime = instance();
		final ContainerController containerController = executorService.submit(
				() -> jadeRuntime.createMainContainer(new ProfileImpl())).get();

		final AgentController rma = containerController.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
		rma.start();

		return containerController;
	}
}
