package org.greencloud.managingsystem.service.planner.plans;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;

public class DisableServerPlanTest {

    @Mock
    ManagingAgent managingAgent;

    @Mock
    ManagingAgentNode managingAgentNode;



    @BeforeEach
    void setup() {
        TimescaleDatabase db = new TimescaleDatabase();
        doReturn(managingAgentNode).when(managingAgent).getAgentNode();
        doReturn(db).when(managingAgentNode).getDatabaseClient();
    }

    @Test
    void xd() {
        DisableServerPlan plan = new DisableServerPlan(managingAgent);
    }
}
