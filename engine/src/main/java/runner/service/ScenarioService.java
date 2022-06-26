package runner.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gui.controller.GUIControllerImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.apache.commons.io.FileUtils;
import runner.domain.ScenarioArgs;
import runner.factory.AgentControllerFactory;
import runner.factory.AgentControllerFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Service used in running the scenarios
 */
public class ScenarioService {

    private static final String RESOURCE_SCENARIO_PATH = "./scenarios/";
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private final AgentControllerFactory factory;
    private final GUIControllerImpl GUIControllerImpl;

    /**
     * Service constructor
     *
     * @param containerController container controller in which agents' controllers are to be created
     */
    public ScenarioService(ContainerController containerController, GUIControllerImpl GUIControllerImpl) {
        this.factory = new AgentControllerFactoryImpl(containerController);
        this.GUIControllerImpl = GUIControllerImpl;
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

            if (Objects.nonNull(scenario.getServerAgentsArgs())) {

                scenario.getAgentsArgs().forEach(agentArgs -> {
                    try {
                        final AgentController agentController = factory.createAgentController(agentArgs);
                        GUIControllerImpl.addAgentNodeToGraph(factory.createAgentNode(agentArgs, scenario));
                        agentController.putO2AObject(GUIControllerImpl, AgentController.ASYNC);
                        agentController.start();
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
