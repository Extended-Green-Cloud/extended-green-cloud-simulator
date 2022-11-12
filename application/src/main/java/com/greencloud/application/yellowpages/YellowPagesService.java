package com.greencloud.application.yellowpages;

import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Service which provides the methods used in communication with the DF
 */
public class YellowPagesService {

	private static final Logger logger = LoggerFactory.getLogger(YellowPagesService.class);

	private YellowPagesService() {
	}

	/**
	 * Method registers the given agent in the DF
	 *
	 * @param agent       agent that is to be registered
	 * @param serviceType type of the service to be registered
	 * @param serviceName name of the service to be registered
	 * @param ownership   name of the owner to be registered
	 */
	public static void register(Agent agent, String serviceType, String serviceName, String ownership) {
		try {
			DFService.register(agent, prepareAgentDescription(agent.getAID(), serviceType, serviceName, ownership));
			DFService.keepRegistered(agent, agent.getDefaultDF(),
					prepareAgentDescription(agent.getAID(), serviceType, serviceName, ownership), null);
		} catch (FIPAException e) {
			logger.info("Couldn't register {} in the directory facilitator", agent);
		}
	}

	/**
	 * Method registers the given agent in the DF
	 *
	 * @param agent       agent that is to be registered
	 * @param serviceType type of the service to be registered
	 * @param serviceName name of the service to be registered
	 */
	public static void register(Agent agent, String serviceType, String serviceName) {
		try {
			DFService.register(agent, prepareAgentDescription(agent.getAID(), serviceType, serviceName));
		} catch (FIPAException e) {
			logger.info("Couldn't register {} in the directory facilitator", agent);
		}
	}

	/**
	 * Method searches the DF for the com.greencloud.application.agents with given service type and ownership
	 *
	 * @param agent       agent which is searching through the DF
	 * @param serviceType type of the service to be searched
	 * @param ownership   name of the owner to be searched
	 * @return list of agent addresses found in DF or empty list if no agents found
	 */
	public static List<AID> search(Agent agent, String serviceType, String ownership) {
		try {
			return Arrays.stream(DFService.search(agent, prepareAgentDescriptionTemplate(serviceType, ownership)))
					.map(DFAgentDescription::getName).toList();
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		return emptyList();
	}

	/**
	 * Method searches the DF for the agents with given service type
	 *
	 * @param agent       agent which is searching through the DF
	 * @param serviceType type of the service to be searched
	 * @return list of agent addresses found in DF
	 */
	public static List<AID> search(Agent agent, String serviceType) {
		try {
			return Arrays.stream(DFService.search(agent, prepareAgentDescriptionTemplate(serviceType)))
					.map(DFAgentDescription::getName).toList();
		} catch (FIPAException e) {
			logger.info("Haven't found any agents because {}", e.getMessage());
		}

		return emptyList();
	}

	/**
	 * Method subscribes a given agent service for the subscriber agent.
	 *
	 * @param subscriber  agent subscribing given service
	 * @param serviceType type of the service to be subscribed
	 * @return subscription ACLMessage
	 */
	public static ACLMessage prepareSubscription(final Agent subscriber, final String serviceType) {
		return DFService.createSubscriptionMessage(subscriber, subscriber.getDefaultDF(),
				prepareAgentDescriptionTemplate(serviceType), null);
	}

	/**
	 * Method decodes the received notification and retrieves newly introduced agents
	 *
	 * @param inform notification received from DF
	 * @return AID list of registered agents
	 */
	public static List<AID> decodeSubscription(final ACLMessage inform) {
		try {
			return Arrays.stream(DFService.decodeNotification(inform.getContent()))
					.map(DFAgentDescription::getName)
					.toList();
		} catch (FIPAException e) {
			logger.info("An error occurred while decoding the notification: {}", e.getMessage());
		}

		return emptyList();
	}

	private static DFAgentDescription prepareAgentDescription(AID aid, String serviceType, String serviceName,
			String ownership) {
		var agentDescription = new DFAgentDescription();
		agentDescription.setName(aid);
		agentDescription.addServices(prepareDescription(serviceType, serviceName, ownership));
		return agentDescription;
	}

	private static DFAgentDescription prepareAgentDescription(AID aid, String serviceType, String serviceName) {
		var agentDescription = new DFAgentDescription();
		agentDescription.setName(aid);
		agentDescription.addServices(prepareDescription(serviceType, serviceName));
		return agentDescription;
	}

	private static ServiceDescription prepareDescription(String serviceType, String serviceName, String ownership) {
		var serviceDescription = new ServiceDescription();
		serviceDescription.setType(serviceType);
		serviceDescription.setName(serviceName);
		serviceDescription.setOwnership(ownership);
		return serviceDescription;
	}

	private static ServiceDescription prepareDescription(String serviceType, String serviceName) {
		var serviceDescription = new ServiceDescription();
		serviceDescription.setType(serviceType);
		serviceDescription.setName(serviceName);
		return serviceDescription;
	}

	private static ServiceDescription prepareDescription(String serviceType) {
		var serviceDescription = new ServiceDescription();
		serviceDescription.setType(serviceType);
		return serviceDescription;
	}

	private static ServiceDescription prepareDescriptionOwnership(String serviceType, String ownership) {
		var serviceDescription = new ServiceDescription();
		serviceDescription.setType(serviceType);
		serviceDescription.setOwnership(ownership);
		return serviceDescription;
	}

	private static DFAgentDescription prepareAgentDescriptionTemplate(String serviceType, String ownership) {
		var template = new DFAgentDescription();
		template.addServices(prepareDescriptionOwnership(serviceType, ownership));
		return template;
	}

	private static DFAgentDescription prepareAgentDescriptionTemplate(String serviceType) {
		var template = new DFAgentDescription();
		template.addServices(prepareDescription(serviceType));
		return template;
	}
}
