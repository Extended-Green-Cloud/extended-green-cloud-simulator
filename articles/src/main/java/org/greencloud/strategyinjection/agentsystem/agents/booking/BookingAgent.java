package org.greencloud.strategyinjection.agentsystem.agents.booking;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.strategyinjection.agentsystem.agents.AbstractAgent;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent representing service for restaurant booking
 */
@SuppressWarnings("unchecked")
public class BookingAgent extends AbstractAgent<BookingNode, BookingProps> {

	private static final Logger logger = LoggerFactory.getLogger(BookingAgent.class);

	@Override
	protected void setup() {
		logger.info("Setting up Agent {}", getName());
		final Object[] arguments = getArguments();

		this.rulesController = (RulesController<BookingProps, BookingNode>) arguments[0];
		this.properties = new BookingProps(getName());
		this.node = new BookingNode(getLocalName());

		setRulesController();
	}

	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}
}
