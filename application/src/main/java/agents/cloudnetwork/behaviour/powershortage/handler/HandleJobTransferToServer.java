package agents.cloudnetwork.behaviour.powershortage.handler;

import static agents.cloudnetwork.behaviour.powershortage.handler.logs.PowerShortageCloudHandlerLog.SERVER_TRANSFER_EXECUTE_TRANSFER_LOG;
import static utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.ClientJob;
import domain.powershortage.PowerShortageJob;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour updates internal cloud network state according to the job transfer
 */
public class HandleJobTransferToServer extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleJobTransferToServer.class);

	private final CloudNetworkAgent myCloudNetworkAgent;
	private final String guid;
	private final String jobId;
	private final AID newServer;

	/**
	 * Behaviour constructor.
	 *
	 * @param myAgent      agent executing the behaviour
	 * @param transferTime time when the power shortage begin
	 * @param jobId        unique identifier of the job
	 * @param newServer    server which will take over the job execution
	 */
	private HandleJobTransferToServer(Agent myAgent, Date transferTime, String jobId, AID newServer) {
		super(myAgent, transferTime);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = myAgent.getName();
		this.jobId = jobId;
		this.newServer = newServer;
	}

	/**
	 * Method creates the HandleJobTransferToServer behaviour based on the passed arguments
	 *
	 * @param cloudNetworkAgent cloud network executing the behaviour
	 * @param powerShortageJob  job to be transferred
	 * @param newServer         server which will take over the job execution
	 * @return behaviour which transfer the jobs between servers
	 */
	public static HandleJobTransferToServer createFor(final CloudNetworkAgent cloudNetworkAgent,
			final PowerShortageJob powerShortageJob, AID newServer) {
		final Instant transferTime = getCurrentTime().isAfter(powerShortageJob.getPowerShortageStart()) ?
				getCurrentTime() :
				powerShortageJob.getPowerShortageStart();
		return new HandleJobTransferToServer(cloudNetworkAgent, Date.from(transferTime),
				powerShortageJob.getJobInstanceId().getJobId(), newServer);
	}

	/**
	 * Method transfers the job between servers.
	 * It updates the internal state of the cloud network
	 */
	@Override
	protected void onWake() {
		final ClientJob jobToExecute = myCloudNetworkAgent.manage().getJobById(jobId);
		if (Objects.nonNull(jobToExecute)) {
			logger.info(SERVER_TRANSFER_EXECUTE_TRANSFER_LOG, guid, jobId, newServer.getLocalName());
			myCloudNetworkAgent.getServerForJobMap().replace(jobId, newServer);
		}
	}
}
