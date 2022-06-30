package com.gui.controller;

import com.gui.domain.nodes.AgentNode;

import java.util.List;

/**
 * Controller for GUI
 */
public interface GUIController {

    /**
     * Method creates the GUI
     */
    void createGUI();

    /**
     * Method adds next agent node to the graph
     *
     * @param agent node of the specified agent
     */
    void addAgentNodeToGraph(final AgentNode agent);

    /**
     * Method creates edges based on existing nodes
     */
    void createEdges();

    /**
     * Method remove agent node from the graph
     *
     * @param agentNode agent node that is to be removed
     */
    void removeAgentNodeFromGraph(final AgentNode agentNode);

    /**
     * Method updates the clients number by given value
     *
     * @param value value to be added to the clients number
     */
    void updateClientsCountByValue(final int value);

    /**
     * Method updates the active jobs number by given value
     *
     * @param value value to be added to the active jobs number
     */
    void updateActiveJobsCountByValue(final int value);

    /**
     * Method updates the all jobs number by given value
     *
     * @param value value to be added to the all jobs number
     */
    void updateAllJobsCountByValue(final int value);

    /**
     * Method adds new information to the latest news panel
     *
     * @param information information that is to be added
     */
    void addNewInformation(final String information);


    /**
     * Method draws an arrow to show the messages flow between agents
     *
     * @param senderAgent    agent sending the message
     * @param receiversNames names of the agents receiving the message
     */
    void displayMessageArrow(final AgentNode senderAgent, final List<String> receiversNames);

}
