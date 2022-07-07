package agents.greenenergy;

import static common.constant.DFServiceConstants.GS_SERVICE_NAME;
import static common.constant.DFServiceConstants.GS_SERVICE_TYPE;
import static yellowpages.YellowPagesService.register;

import agents.cloudnetwork.behaviour.powershortage.listener.ListenForServerPowerShortage;
import agents.greenenergy.behaviour.ReceivePowerRequest;
import agents.greenenergy.behaviour.listener.ListenForFinishedJobs;
import agents.greenenergy.behaviour.listener.ListenForGreenSourceEvent;
import agents.greenenergy.behaviour.listener.ListenForStartedJobs;
import agents.greenenergy.behaviour.powershortage.listener.ListenForParentServerPowerShortage;
import agents.greenenergy.behaviour.powershortage.listener.ListenForPowerTransferCancellation;
import agents.greenenergy.behaviour.powershortage.listener.ListenForTransferConfirmation;
import agents.greenenergy.domain.EnergyTypeEnum;
import agents.greenenergy.domain.GreenEnergyStateManagement;
import agents.greenenergy.domain.GreenPower;
import behaviours.ReceiveGUIController;
import domain.location.ImmutableLocation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
        addBehaviour(new ReceiveGUIController(this, behavioursRunAtStart()));
    }

    @Override
    protected void takeDown() {
        getGuiController().removeAgentNodeFromGraph(getAgentNode());
        super.takeDown();
    }

    private void initializeAgent(final Object[] args) {
        if (Objects.nonNull(args) && args.length == 7) {
            this.powerJobs = new ConcurrentHashMap<>();
            this.monitoringAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
            this.ownerServer = new AID(args[1].toString(), AID.ISLOCALNAME);
            this.stateManagement = new GreenEnergyStateManagement(this);
            try {
                this.greenPower = new GreenPower(Integer.parseInt(args[2].toString()), this);
                this.pricePerPowerUnit = Double.parseDouble(args[3].toString());
                this.location = ImmutableLocation.builder()
                        .latitude(Double.parseDouble(args[4].toString()))
                        .longitude(Double.parseDouble(args[5].toString()))
                        .build();
                this.energyType = (EnergyTypeEnum) args[6];
            } catch (NumberFormatException e) {
                logger.info("Incorrect argument: please check arguments in the documentation");
                doDelete();
            }
        } else {
            logger.info("Incorrect arguments: some parameters for green source agent are missing - check the parameters in the documentation");
            doDelete();
        }
    }

    private List<Behaviour> behavioursRunAtStart() {
        final List<Behaviour> behaviours = new ArrayList<>();
        behaviours.add(new ReceivePowerRequest(this));
        behaviours.add(new ListenForFinishedJobs(this));
        behaviours.add(new ListenForStartedJobs(this));
        behaviours.add(new ListenForGreenSourceEvent(this));
        behaviours.add(new ListenForPowerTransferCancellation(this));
        behaviours.add(new ListenForTransferConfirmation(this));
        behaviours.add(new ListenForParentServerPowerShortage(this));
        return behaviours;
    }
}