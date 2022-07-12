package runner.service;

import static runner.service.domain.ScenarioConstants.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gui.controller.GUIControllerImpl;
import com.gui.domain.nodes.AgentNode;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.apache.commons.io.FileUtils;
import runner.domain.AgentArgs;
import runner.domain.ClientAgentArgs;
import runner.domain.ImmutableClientAgentArgs;
import runner.domain.ScenarioArgs;
import runner.factory.AgentControllerFactory;
import runner.factory.AgentControllerFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Service used in running the scenarios
 */
public class ScenarioService {

    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private static final List<AgentController> AGENTS_TO_RUN = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private final AgentControllerFactory factory;
    private final GUIControllerImpl guiController;


    /**
     * Service constructor
     *
     * @param containerController container controller in which agents' controllers are to be created
     */
    public ScenarioService(ContainerController containerController, GUIControllerImpl guiController) {
        this.factory = new AgentControllerFactoryImpl(containerController);
        this.guiController = guiController;
    }

    /**
     * Method creates the agent controllers that are to be run from the xml file
     *
     * @param fileName XML file containing the scenario description
     */
    public void createAgentsFromScenarioFile(final String fileName) {
        final File scenarioFile = getFileFromResourceFileName(fileName);
        try {
            final ScenarioArgs scenario = XML_MAPPER.readValue(scenarioFile, ScenarioArgs.class);

            if (Objects.nonNull(scenario.getAgentsArgs())) {
                createAgents(scenario.getMonitoringAgentsArgs(), scenario);
                createAgents(scenario.getGreenEnergyAgentsArgs(), scenario);
                createAgents(scenario.getServerAgentsArgs(), scenario);
                createAgents(scenario.getCloudNetworkAgentsArgs(), scenario);
            }
            guiController.createEdges();
            // next line is added on purpose! It waits for the graph to fully initialize
            TimeUnit.SECONDS.sleep(7);
            for (AgentController agentController : AGENTS_TO_RUN) {
                agentController.start();
                agentController.activate();
                TimeUnit.MILLISECONDS.sleep(100);
            }
            switch (scenario.getClientGenerationType()){
                case FromScenraio -> createAgents(scenario.getClientAgentsArgs(), scenario);
                case OnStart -> createClientAgents(CLIENT_NUMBER, scenario);
                case Continuous -> keepSpawningAgents(scenario);
            }

        } catch (IOException | InterruptedException | StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void createAgents(List<?> agentArgsList, ScenarioArgs scenario) {
        agentArgsList.forEach(agentArgs -> {
            var args = (AgentArgs) agentArgs;
            try {
                final AgentController agentController = factory.createAgentController(args);
                final AgentNode agentNode = factory.createAgentNode(args, scenario);
                guiController.addAgentNodeToGraph(agentNode);
                agentController.putO2AObject(guiController, AgentController.ASYNC);
                agentController.putO2AObject(agentNode, AgentController.ASYNC);
                AGENTS_TO_RUN.add(agentController);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        });
    }

    private void createClientAgents(final int agentsNumber, final ScenarioArgs scenario) {
        IntStream.rangeClosed(1, agentsNumber).forEach(idx -> {
            final int randomPower = MIN_JOB_POWER + RANDOM.nextInt(MAX_JOB_POWER);
            final int randomStart = START_TIME_MIN + RANDOM.nextInt(START_TIME_MAX);
            final int randomEnd = randomStart + 1 + RANDOM.nextInt(END_TIME_MAX);
            final ClientAgentArgs clientAgentArgs =
                    ImmutableClientAgentArgs.builder()
                            .name(String.format("Client%d", idx))
                            .jobId(String.valueOf(idx))
                            .power(String.valueOf(randomPower))
                            .start(String.valueOf(randomStart))
                            .end(String.valueOf(randomEnd))
                            .build();
            try {
                final AgentController agentController = factory.createAgentController(clientAgentArgs);
                final AgentNode agentNode = factory.createAgentNode(clientAgentArgs, scenario);
                guiController.addAgentNodeToGraph(agentNode);
                agentController.putO2AObject(guiController, AgentController.ASYNC);
                agentController.putO2AObject(agentNode, AgentController.ASYNC);
                agentController.start();
                agentController.activate();
            } catch (StaleProxyException  e) {
                e.printStackTrace();
            }
        });
    }

    private void keepSpawningAgents(ScenarioArgs scenario){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(new ClientSpawner(factory, guiController, scenario));
        executorService.shutdown();
    }

    private File getFileFromResourceFileName(final String fileName) {
        try {
            final InputStream inputStream = ScenarioService.class.getClassLoader()
                    .getResourceAsStream(RESOURCE_SCENARIO_PATH + fileName + ".xml");
            final File tempFile;
            tempFile = File.createTempFile(fileName, ".xml");
            if (Objects.nonNull(inputStream)) {
                FileUtils.copyToFile(inputStream, tempFile);
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
