package com.greencloud.application.agents.monitoring.behaviour.templates;

import static jade.lang.acl.ACLMessage.REQUEST;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in behaviours that server weather information
 */
public class WeatherServingMessageTemplates {

	public static final MessageTemplate SERVE_FORECAST_TEMPLATE  = MessageTemplate.MatchPerformative(REQUEST);
}
