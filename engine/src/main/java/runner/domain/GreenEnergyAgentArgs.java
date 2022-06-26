package runner.domain;

import agents.greenenergy.domain.EnergyTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Arguments for the Green Source Agent
 */
@JsonSerialize(as = ImmutableGreenEnergyAgentArgs.class)
@JsonDeserialize(as = ImmutableGreenEnergyAgentArgs.class)
@Value.Immutable
public interface GreenEnergyAgentArgs extends AgentArgs{

    /**
     * @return owned monitoring agent name
     */
    String getMonitoringAgent();

    /**
     * @return owner server name
     */
    String getOwnerSever();

    /**
     * @return location's latitude
     */
    String getLatitude();

    /**
     * @return location's longitude
     */
    String getLongitude();

    /**
     * @return price for 1kWh
     */
    String getPricePerPowerUnit();

    /**
     * @return maximum capacity of the server
     */
    String getMaximumCapacity();

    /**
     * @return type of energy source
     */
    EnergyTypeEnum getEnergyType();
}
