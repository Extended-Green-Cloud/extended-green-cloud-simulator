package runner.service;

import com.gui.controller.GUIControllerImpl;
import com.gui.domain.nodes.AgentNode;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import runner.domain.AgentArgs;
import runner.domain.ClientAgentArgs;
import runner.domain.ImmutableClientAgentArgs;
import runner.domain.ScenarioArgs;
import runner.factory.AgentControllerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static runner.service.domain.ScenarioConstants.*;

/**
 * Service that periodically spawns client agents
 */
public class ClientSpawner implements Runnable {

    private static final int INTERVAL = 3;
    private static final int AGENTS_PER_TICK = 2;
    private static final Random RANDOM = new Random();

    private final AgentControllerFactory factory;
    private final GUIControllerImpl guiController;

    private final ScenarioArgs scenario;

    public ClientSpawner(AgentControllerFactory factory, GUIControllerImpl guiController, ScenarioArgs scenario){
        this.factory = factory;
        this.guiController = guiController;
        this.scenario = scenario;
    }

    @Override
    public void run() {
        int clientId = 1;
        while(true){
            int finalClientId = clientId;
            IntStream.rangeClosed(1, AGENTS_PER_TICK).forEach(idx -> {
                final int randomPower = MIN_JOB_POWER + RANDOM.nextInt(MAX_JOB_POWER);
                final int randomStart = START_TIME_MIN + RANDOM.nextInt(START_TIME_MAX);
                final int randomEnd = randomStart + 1 + RANDOM.nextInt(END_TIME_MAX);
                final ClientAgentArgs clientAgentArgs =
                        ImmutableClientAgentArgs.builder()
                                .name(String.format("Client%d", idx % 2 == 1 ? finalClientId : finalClientId + 1))
                                .jobId(String.valueOf(idx % 2 == 1 ? finalClientId : finalClientId + 1))
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
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            });
            clientId +=2;
            try {
                TimeUnit.SECONDS.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
