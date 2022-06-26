package agents.greenenergy;

import static common.constant.DFServiceConstants.GS_SERVICE_NAME;
import static common.constant.DFServiceConstants.GS_SERVICE_TYPE;
import static yellowpages.YellowPagesService.register;

import agents.greenenergy.behaviour.ListenForFinishedJobs;
import agents.greenenergy.behaviour.ReceivePowerRequest;
import domain.location.ImmutableLocation;
import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

/**
 * Agent representing the Green Energy Source Agent that produces the power for the Servers
 */
public class GreenEnergyAgent extends AbstractGreenEnergyAgent {

    private static final Logger logger = LoggerFactory.getLogger(GreenEnergyAgent.class);

    /**
     * Method run at the agent's start. In initialize the Green Source Agent based on the given by the user arguments,
     * registers it in the DF and then runs the starting behaviours - listening for the power requests and listening for
     * the finish power request information.
     */
    @Override
    protected void setup() {
        super.setup();
        final Object[] args = getArguments();
        initializeAgent(args);
        register(this, GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerServer.getName());
        addBehaviour(new ReceivePowerRequest(this));
    }

    private void initializeAgent(final Object[] args) {
        if (Objects.nonNull(args) && args.length == 6) {

            this.powerJobs = new HashMap<>();
            this.monitoringAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
            this.ownerServer = new AID(args[1].toString(), AID.ISLOCALNAME);
            try {
                this.maximumCapacity = Integer.parseInt(args[2].toString());
                this.pricePerPowerUnit = Double.parseDouble(args[3].toString());
                this.location = ImmutableLocation.builder()
                        .latitude(Double.parseDouble(args[4].toString()))
                        .longitude(Double.parseDouble(args[5].toString()))
                        .build();
            } catch (NumberFormatException e) {
                logger.info("Incorrect argument: please check arguments in the documentation");
                doDelete();
            }
        } else {
            logger.info("Incorrect arguments: some parameters for green source agent are missing - check the parameters in the documentation");
            doDelete();
        }
    }
}