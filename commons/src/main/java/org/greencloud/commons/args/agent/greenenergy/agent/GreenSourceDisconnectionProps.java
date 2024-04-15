package org.greencloud.commons.args.agent.greenenergy.agent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Class represents parameters set for a Green Source if it is undergoing the disconnection from a given Server
 */
@Getter
@Setter
public class GreenSourceDisconnectionProps {

	private AID serverToBeDisconnected;
	private ACLMessage originalAdaptationMessage;
	private AtomicBoolean isBeingDisconnected;

	/**
	 * Default constructor
	 */
	public GreenSourceDisconnectionProps() {
		this.reset();
	}

	/**
	 * @return boolean indicating if full Green Source disconnection is ongoing
	 */
	public boolean isBeingDisconnectedFromServer() {
		return isBeingDisconnected.get() && Objects.nonNull(serverToBeDisconnected);
	}

	/**
	 * Method sets the initial state of Green Source disconnection
	 */
	public void reset() {
		this.serverToBeDisconnected = null;
		this.originalAdaptationMessage = null;
		this.isBeingDisconnected = new AtomicBoolean(false);
	}

	public boolean isBeingDisconnected() {
		return isBeingDisconnected.get();
	}
	public void setBeingDisconnected(boolean beingDisconnected) {
		isBeingDisconnected.set(beingDisconnected);
	}
}
