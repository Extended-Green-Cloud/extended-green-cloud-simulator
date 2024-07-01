package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ALLOCATION_DATA_REQUEST;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import java.util.Collection;

import org.greencloud.commons.domain.allocation.ImmutableAllocatedJobs;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class RequestMessageFactory {

	/**
	 * Method constructs a message sent when requesting data for jobs allocation.
	 *
	 * @param facts     facts
	 * @param receivers receivers of the message
	 * @return REQUEST ACLMessage
	 */
	public static ACLMessage requestDataForAllocation(final RuleSetFacts facts, final Collection<AID> receivers) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(ALLOCATION_DATA_REQUEST)
				.withObjectContent(mapToAllocatedJobs(facts.get(JOBS)))
				.withReceivers(receivers)
				.build();
	}

	/**
	 * Method constructs a message sent when requesting data for jobs allocation.
	 *
	 * @param facts     facts
	 * @param receivers receivers of the message
	 * @return REQUEST ACLMessage
	 */
	public static ACLMessage requestDataForAllocationAllocatedJobs(final RuleSetFacts facts,
			final Collection<AID> receivers) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(ALLOCATION_DATA_REQUEST)
				.withObjectContent(mapToAllocatedJobs(((ImmutableAllocatedJobs) facts.get(JOBS)).getAllocationJobs()))
				.withReceivers(receivers)
				.build();
	}
}
