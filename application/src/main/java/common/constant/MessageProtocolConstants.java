package common.constant;

/**
 * Constants describing the message protocols.
 */
public class MessageProtocolConstants {

    /**
     * Call for proposals protocols
     * CLIENT_JOB_CFP_PROTOCOL   - protocol used in client's call for proposal message that is sent to cloud networks while looking
     *                             for the network for job execution
     * CNA_JOB_CFP_PROTOCOL      - protocol used in cloud network's call for proposal message that is sent to the servers while
     *                             looking for the server which will execute the job
     * SERVER_JOB_CFP_PROTOCOL   - protocol used in the server's call for proposal message that is sent to the green sources while
     *                             looking for the green source which will supply the server with power needed for the job execution
     */
    public static final String CLIENT_JOB_CFP_PROTOCOL = "CLIENT_JOB_CFP";
    public static final String CNA_JOB_CFP_PROTOCOL = "CNA_JOB_CFP";
    public static final String SERVER_JOB_CFP_PROTOCOL = "SERVER_JOB_CFP";

    /**
     * Job status protocols
     * FINISH_JOB_PROTOCOL         - protocol used in messages informing the agent that the job execution has finished
     * MANUAL_JOB_FINISH_PROTOCOL  - protocol used in messages informing that the job had to be finished manually because the time
     *                               after which the information about job being finished has passed
     * STARTED_JOB_PROTOCOL        - protocol used in messages informing the agent that the job execution has started
     * DELAYED_JOB_PROTOCOL        - protocol used in messages informing that the job execution started with the delay
     * BACK_UP_POWER_JOB_PROTOCOL  - protocol used in messages informing that the job is executed using the back-up power
     */
    public static final String FINISH_JOB_PROTOCOL = "FINISH";
    public static final String MANUAL_JOB_FINISH_PROTOCOL = "MANUAL_FINISH";
    public static final String STARTED_JOB_PROTOCOL = "STARTED";
    public static final String DELAYED_JOB_PROTOCOL = "DELAYED";
    public static final String BACK_UP_POWER_JOB_PROTOCOL = "BACK_UP_POWER_JOB_PROTOCOL";


    /**
     * Power shortage protocols
     * POWER_SHORTAGE_ALERT_PROTOCOL                - protocol used in messages informing the agent that the power shortage was detected
     * POWER_SHORTAGE_SOURCE_TRANSFER_PROTOCOL      - protocol used in messages informing the agent that the transfer of the job between green
     *                                                sources caused by the power shortage was established
     * POWER_SHORTAGE_SERVER_TRANSFER_PROTOCOL      - protocol used in messages informing the agent that the transfer of the job between servers
     *                                                caused by the power shortage was established
     * POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL       - protocol used in power confirmation messages sent by the green sources indicating that the power
     *                                                is sent to supply the jobs coming from transfer
     * CANCELLED_TRANSFER_PROTOCOL                  - protocol informing that the job transfer should be cancelled
     */
    public static final String POWER_SHORTAGE_ALERT_PROTOCOL = "POWER_SHORTAGE_ALERT_PROTOCOL";
    public static final String POWER_SHORTAGE_SOURCE_TRANSFER_PROTOCOL = "POWER_SHORTAGE_TRANSFER_PROTOCOL";
    public static final String POWER_SHORTAGE_SERVER_TRANSFER_PROTOCOL = "POWER_SHORTAGE_SERVER_TRANSFER_PROTOCOL";
    public static final String POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL = "POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL";
    public static final String CANCELLED_TRANSFER_PROTOCOL = "CANCELLED_TRANSFER_PROTOCOL";

}
