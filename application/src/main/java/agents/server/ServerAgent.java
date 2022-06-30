package agents.server;

import static common.constant.DFServiceConstants.*;
import static yellowpages.YellowPagesService.register;
import static yellowpages.YellowPagesService.search;

import agents.server.behaviour.ReceiveJobRequest;
import behaviours.ReceiveGUIController;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Agent representing the Server Agent which executes the clients' jobs
 */
public class ServerAgent extends AbstractServerAgent {

    private static final Logger logger = LoggerFactory.getLogger(ServerAgent.class);

    /**
     * Method run at the agent's start. In initialize the Server Agent based on the given by the user arguments,
     * registers it in the DF and then runs the starting behaviours - listening for the job requests
     */
    @Override
    protected void setup() {
        super.setup();
        final Object[] args = getArguments();
        initializeAgent(args);
        register(this, SA_SERVICE_TYPE, SA_SERVICE_NAME, ownerCloudNetworkAgent.getName());
        addBehaviour(new ReceiveGUIController(this, List.of(new ReceiveJobRequest())));
    }

    @Override
    protected void takeDown() {
        logger.info("I'm finished. Bye!");
        getGuiController().removeAgentNodeFromGraph(getAgentNode());
        super.takeDown();
    }

    private void initializeAgent(final Object[] args) {
        if (Objects.nonNull(args) && args.length == 3) {
            this.ownedGreenSources = search(this, GS_SERVICE_TYPE, getName());
            if (ownedGreenSources.isEmpty()) {
                logger.info("I have no corresponding green sources!");
                doDelete();
            }
            this.ownerCloudNetworkAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
            try {
                this.pricePerHour = Double.parseDouble(args[1].toString());
                this.maximumCapacity = Integer.parseInt(args[2].toString());
            } catch (final NumberFormatException e) {
                logger.info("The given price is not a number!");
                doDelete();
            }
        } else {
            logger.info("Incorrect arguments: some parameters for server agent are missing - check the parameters in the documentation");
            doDelete();
        }

    }
}
