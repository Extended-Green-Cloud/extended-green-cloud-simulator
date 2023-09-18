package org.greencloud.commons.args.agent.greenenergy.agent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class represents parameters set for a Green Source if it is undergoing the disconnection from a given Server
 */
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

	public AID getServerToBeDisconnected() {
		return serverToBeDisconnected;
	}
	public ACLMessage getOriginalAdaptationMessage() {
		return originalAdaptationMessage;
	}
	public boolean isBeingDisconnected() {
		return isBeingDisconnected.get();
	}
	public void setServerToBeDisconnected(AID serverToBeDisconnected) {
		this.serverToBeDisconnected = serverToBeDisconnected;
	}
	public void setOriginalAdaptationMessage(ACLMessage originalAdaptationMessage) {
		this.originalAdaptationMessage = originalAdaptationMessage;
	}
	public void setBeingDisconnected(boolean beingDisconnected) {
		isBeingDisconnected.set(beingDisconnected);
	}
}
