package domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Object storing the data passed by the Green Source
 */
@JsonSerialize(as = ImmutableGreenSourceData.class)
@JsonDeserialize(as = ImmutableGreenSourceData.class)
@Immutable
public interface GreenSourceData {

    /**
     * @return available power at the given time
     */
    double getAvailablePowerInTime();

    /**
     * @return price for the 1kWh
     */
    double getPricePerPowerUnit();

    /**
     * @return unique identifier of the given job of interest
     */
    String getJobId();
}