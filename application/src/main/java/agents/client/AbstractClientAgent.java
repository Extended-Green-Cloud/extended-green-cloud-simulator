package agents.client;

import agents.AbstractAgent;
import jade.core.AID;

/**
 * Abstract agent class storing the data regarding Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent {

    protected AID chosenCloudNetworkAgent;

    public AbstractClientAgent() {
        super.setup();
    }

    /**
     * Abstract Client Agent constructor.
     *
     * @param chosenCloudNetworkAgent Cloud Network Agent that was chosen for the job execution
     */
    public AbstractClientAgent(AID chosenCloudNetworkAgent) {
        this.chosenCloudNetworkAgent = chosenCloudNetworkAgent;
    }

    public AID getChosenCloudNetworkAgent() {
        return chosenCloudNetworkAgent;
    }

    public void setChosenCloudNetworkAgent(AID chosenCloudNetworkAgent) {
        this.chosenCloudNetworkAgent = chosenCloudNetworkAgent;
    }
}
