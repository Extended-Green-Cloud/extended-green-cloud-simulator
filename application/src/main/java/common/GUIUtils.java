package common;

import agents.AbstractAgent;
import agents.cloudnetwork.CloudNetworkAgent;
import agents.greenenergy.GreenEnergyAgent;
import agents.server.ServerAgent;
import com.gui.domain.nodes.CloudNetworkAgentNode;
import com.gui.domain.nodes.GreenEnergyAgentNode;
import com.gui.domain.nodes.ServerAgentNode;
import jade.core.AID;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class defines set of utilities used together with GUI Controller
 */
public class GUIUtils {

    /**
     * Method updates the GUI to indicate that the new job has started in cloud network
     *
     * @param agent agent updating the GUI
     */
    public static void announceStartedJob(final CloudNetworkAgent agent) {
        final CloudNetworkAgentNode cloudNetworkAgentNode = (CloudNetworkAgentNode) agent.getAgentNode();
        agent.getGuiController().updateActiveJobsCountByValue(1);
        cloudNetworkAgentNode.updateJobsCount(1);
        cloudNetworkAgentNode.updateClientNumber(1);
        cloudNetworkAgentNode.updateTraffic(agent.getCurrentPowerInUse());
    }

    /**
     * Method updates the GUI to indicate that the job execution has finished
     *
     * @param agent agent updating the GUI
     * @param jobId unique identifier of the GUI
     */
    public static void announceFinishedJob(final CloudNetworkAgent agent, final String jobId) {
        final CloudNetworkAgentNode cloudNetworkAgentNode = (CloudNetworkAgentNode) agent.getAgentNode();
        final String information = String.format("Execution of the job %s has finished!", jobId);
        agent.getGuiController().updateActiveJobsCountByValue(-1);
        agent.getGuiController().updateAllJobsCountByValue(-1);
        agent.getGuiController().addNewInformation(information);
        cloudNetworkAgentNode.updateJobsCount(-1);
        cloudNetworkAgentNode.updateClientNumber(-1);
        cloudNetworkAgentNode.updateTraffic(agent.getCurrentPowerInUse());
    }

    /**
     * Method updates the GUI when the green source changes the state (finishes the job or starts the job)
     *
     * @param agent         agent updating the GUI
     * @param isJobFinished flag indicating if the state update is caused by finished or started job
     */
    public static void updateGreenSourceState(final GreenEnergyAgent agent, final boolean isJobFinished) {
        final GreenEnergyAgentNode greenEnergyAgentNode = (GreenEnergyAgentNode) agent.getAgentNode();
        greenEnergyAgentNode.updateJobsCount(isJobFinished? -1 : 1);
        greenEnergyAgentNode.updateIsActive(agent.getIsActiveState());
        greenEnergyAgentNode.updateTraffic(agent.getCurrentPowerInUse());
    }

    /**
     * Method updates the GUI when the server changes the state (finishes the job or starts the job)
     *
     * @param agent         agent updating the GUI
     * @param isJobFinished flag indicating if the state update is caused by finished or started job
     */
    public static void updateServerState(final ServerAgent agent, final boolean isJobFinished) {
        final ServerAgentNode serverAgentNode = (ServerAgentNode) agent.getAgentNode();
        serverAgentNode.updateJobsCount(isJobFinished? -1 : 1);
        serverAgentNode.updateClientNumber(isJobFinished? -1 : 1);
        serverAgentNode.updateIsActive(agent.getIsActiveState());
        serverAgentNode.updateTraffic(agent.getCurrentPowerInUse());
    }

    /**
     * Method updates the GUI to indicate that a new job is planned to be executed
     *
     * @param agent agent updating the GUI
     * @param jobId unique identifier of the GUI
     */
    public static void announceBookedJob(final AbstractAgent agent, final String jobId) {
        agent.getGuiController().updateAllJobsCountByValue(1);
    }

    /**
     * Method updates the GUI to indicate that new client is using Cloud Network
     *
     * @param agent agent updating the GUI
     */
    public static void announceNewClient(final AbstractAgent agent) {
        final String information = "New client in Cloud Network!";
        agent.getGuiController().updateClientsCountByValue(1);
        agent.getGuiController().addNewInformation(information);
    }

    /**
     * Method displays the message arrow in GUI
     *
     * @param agent    agent being the sender
     * @param receiver address of the receiver
     */
    public static void displayMessageArrow(final AbstractAgent agent, final AID receiver) {
        agent.getGuiController().displayMessageArrow(agent.getAgentNode(), Collections.singletonList(receiver.getLocalName()));
    }

    /**
     * Method displays the message arrow in GUI
     *
     * @param agent     agent being the sender
     * @param receivers addresses of the receivers
     */
    public static void displayMessageArrow(final AbstractAgent agent, final List<AID> receivers) {
        agent.getGuiController().displayMessageArrow(agent.getAgentNode(), receivers.stream().map(AID::getLocalName).toList());
    }

    /**
     * Method displays the message arrow in GUI
     *
     * @param agent     agent being the sender
     * @param receivers iterator of addresses of the receivers
     */
    public static void displayMessageArrow(final AbstractAgent agent, final Iterator<AID> receivers) {
        final Iterable<AID> iterable = () -> receivers;
        final Stream<AID> stream = StreamSupport.stream(iterable.spliterator(), false);
        agent.getGuiController().displayMessageArrow(agent.getAgentNode(), stream.map(AID::getLocalName).toList());
    }
}
