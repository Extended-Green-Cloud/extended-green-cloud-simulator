package com.greencloud.application.agents.greenenergy.domain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class represents parameters set for a Green Source if it is undergoing the disconnection from a given Server
 */
public class GreenSourceDisconnection {

	private AID serverToBeDisconnected;
	private ACLMessage originalAdaptationMessage;
	private AtomicBoolean isBeingDisconnected;

	/**
	 * Default constructor
	 */
	public GreenSourceDisconnection() {
		this.reset();
	}

	/**
	 * Constructor (mostly used in tests)
	 *
	 * @param serverToBeDisconnected    server with which Green Source is disconnected
	 * @param originalAdaptationMessage received adaptation message awaiting a response
	 * @param isBeingDisconnected       flag indicating that disconnection is going on
	 */
	public GreenSourceDisconnection(AID serverToBeDisconnected, ACLMessage originalAdaptationMessage,
			boolean isBeingDisconnected) {
		this.serverToBeDisconnected = serverToBeDisconnected;
		this.originalAdaptationMessage = originalAdaptationMessage;
		this.isBeingDisconnected = new AtomicBoolean(isBeingDisconnected);
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

	public void setServerToBeDisconnected(AID serverToBeDisconnected) {
		this.serverToBeDisconnected = serverToBeDisconnected;
	}

	public ACLMessage getOriginalAdaptationMessage() {
		return originalAdaptationMessage;
	}

	public void setOriginalAdaptationMessage(ACLMessage originalAdaptationMessage) {
		this.originalAdaptationMessage = originalAdaptationMessage;
	}

	public boolean isBeingDisconnected() {
		return isBeingDisconnected.get();
	}

	public void setBeingDisconnected(boolean beingDisconnected) {
		isBeingDisconnected.set(beingDisconnected);
	}
}
