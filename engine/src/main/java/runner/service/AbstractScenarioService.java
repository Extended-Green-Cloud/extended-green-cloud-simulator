package runner.service;

import static jade.core.Runtime.instance;
import static jade.wrapper.AgentController.ASYNC;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;
import static org.greencloud.commons.utils.time.TimeSimulation.setSystemStartTime;
import static runner.configuration.EngineConfiguration.containerId;
import static runner.configuration.EngineConfiguration.databaseHostIp;
import static runner.configuration.EngineConfiguration.jadeInterPort;
import static runner.configuration.EngineConfiguration.jadeIntraPort;
import static runner.configuration.EngineConfiguration.localHostIp;
import static runner.configuration.EngineConfiguration.locationId;
import static runner.configuration.EngineConfiguration.mainHost;
import static runner.configuration.EngineConfiguration.mainHostIp;
import static runner.configuration.EngineConfiguration.newPlatform;
import static runner.configuration.EngineConfiguration.platformId;
import static runner.configuration.EngineConfiguration.runJadeGUI;
import static runner.configuration.EngineConfiguration.runJadeSniffer;
import static runner.configuration.EngineConfiguration.websocketAddresses;
import static runner.configuration.enums.ContainerTypeEnum.CLIENTS_CONTAINER_ID;
import static runner.constants.EngineConstants.MTP_MULTI_MESSAGE;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.InvalidScenarioException;
import org.greencloud.commons.exception.JadeContainerException;
import org.greencloud.commons.exception.JadeControllerException;
import org.greencloud.rulescontroller.RulesController;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.greencloud.connector.factory.AgentControllerFactory;
import com.greencloud.connector.factory.AgentNodeFactoryImpl;
import com.greencloud.connector.gui.GuiController;
import com.greencloud.connector.gui.GuiControllerImpl;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Abstract class serving as common base to Single and Multi Scenario Services.
 * It handles creation of Main and Agent's Containers as well as Agent's Controllers.
 * It is also responsible for running Agent's and Agent's clients.
 */
public abstract class AbstractScenarioService {

	protected static final ExecutorService executorService = Executors.newCachedThreadPool();
	protected final GuiController guiController;
	protected final TimescaleDatabase timescaleDatabase;
	protected final Runtime jadeRuntime;
	protected final ContainerController mainContainer;
	protected final ContainerController agentContainer;

	protected AgentControllerFactory factory;
	protected ScenarioStructureArgs scenario;
	protected Map<String, Map<String, Object>> systemKnowledge;
	protected ScenarioWorkloadGenerationService workloadGenerator;

	/**
	 * Constructor called by {@link MultiContainerScenarioService} and {@link SingleContainerScenarioService}
	 * Launches gui and the main controller.
	 * In case of MultiContainer case runs environment only for the main host.
	 */
	protected AbstractScenarioService()
			throws ExecutionException, InterruptedException, StaleProxyException {
		this.workloadGenerator = new ScenarioWorkloadGenerationService(this);
		this.jadeRuntime = instance();
		this.timescaleDatabase = new TimescaleDatabase(databaseHostIp);
		this.guiController = new GuiControllerImpl(websocketAddresses);

		if (mainHost) {
			timescaleDatabase.initDatabase();
		}

		executorService.execute(guiController);
		mainContainer = (newPlatform || mainHost) ? runMainContainer() : null;
		agentContainer = mainHost ? null : runAgentsContainer();

		if (runJadeGUI && (mainHost || newPlatform)) {
			runJadeGui();
		}
	}

	protected ScenarioStructureArgs parseScenarioStructure(final File scenarioStructureFile) {
		try {
			return getMapper().readValue(scenarioStructureFile, ScenarioStructureArgs.class);
		} catch (IOException e) {
			throw new InvalidScenarioException(
					format("Failed to parse scenario structure file \"%s\"", scenarioStructureFile), e);
		}
	}

	protected Map<String, Map<String, Object>> parseKnowledgeStructure(final File knowledgeFile) {
		try {
			final TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<>() {
			};
			return getMapper().readValue(knowledgeFile, typeRef);
		} catch (IOException e) {
			throw new InvalidScenarioException(
					format("Failed to parse knowledge file \"%s\"", knowledgeFile), e);
		}
	}

	protected AgentController prepareManagingController(final ManagingAgentArgs managingAgentArgs) {
		try {
			var managingNode = new AgentNodeFactoryImpl().createAgentNode(managingAgentArgs, scenario);
			var managingAgent = mainContainer.createNewAgent(managingAgentArgs.getName(),
					"org.greencloud.managingsystem.agent.ManagingAgent",
					new Object[] { managingAgentArgs.getSystemQualityThreshold(),
							scenario,
							mainContainer,
							guiController,
							managingAgentArgs.getPowerShortageThreshold(),
							managingAgentArgs.getDisabledActions()
					});

			final RulesController rulesController = new RulesController();
			managingNode.setDatabaseClient(timescaleDatabase);
			guiController.addAgentNodeToGraph(managingNode);
			managingAgent.putO2AObject(guiController, ASYNC);
			managingAgent.putO2AObject(managingNode, ASYNC);
			managingAgent.putO2AObject(rulesController, ASYNC);

			return managingAgent;
		} catch (StaleProxyException e) {
			throw new JadeControllerException("Failed to run managing agent controller", e);
		}
	}

	private ContainerController runMainContainer() throws ExecutionException, InterruptedException {
		final Profile profile = new ProfileImpl(localHostIp, Integer.parseInt(jadeIntraPort), platformId, true);
		profile.setParameter(Profile.ACCEPT_FOREIGN_AGENTS, "true");
		profile.setParameter(MTP_MULTI_MESSAGE, newPlatform ? "false" : "true");

		if (localHostIp != null) {
			final String platformAddress = format("http://%s:%s/acc", localHostIp, jadeInterPort);
			profile.setParameter(Profile.MTPS, format("jade.mtp.http.MessageTransportProtocol(%s)", platformAddress));
			timescaleDatabase.writeAMSData("ams@" + platformId, platformAddress);
		}
		return executorService.submit(() -> jadeRuntime.createMainContainer(profile)).get();
	}

	private ContainerController runAgentsContainer() {
		final String platformHost = newPlatform && isNull(containerId) ? localHostIp : mainHostIp;
		final String containerName =
				newPlatform || isNull(containerId) ? defaultIfNull(locationId, "CNA") : containerId;

		var profile = new ProfileImpl(platformHost, Integer.parseInt(jadeIntraPort), platformId, false);
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.ACCEPT_FOREIGN_AGENTS, "true");
		profile.setParameter(MTP_MULTI_MESSAGE, "false");
		profile.setParameter(Profile.EXPORT_HOST, localHostIp);

		try {
			return executorService.submit(() -> jadeRuntime.createAgentContainer(profile)).get();
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			if (containerName.equals(CLIENTS_CONTAINER_ID.toString())) {
				throw new JadeContainerException("Failed to create Agent Clients container", e);
			}
			throw new JadeContainerException("Failed to create CloudNetwork container", e);
		}
	}

	private void runJadeGui() throws StaleProxyException {
		final AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
		rma.start();

		if (runJadeSniffer) {
			final AgentController sniffer = mainContainer.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",
					new Object[0]);
			sniffer.start();
		}
	}

	protected void updateSystemStartTime() {
		final Instant systemStart = timescaleDatabase.readSystemStartTime();
		setSystemStartTime(systemStart);
		guiController.reportSystemStartTime(systemStart);
	}

}
