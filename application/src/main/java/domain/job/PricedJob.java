package domain.job;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Object storing the data describing job and the cost of its execution
 */
@JsonDeserialize(as = ImmutablePricedJob.class)
@JsonSerialize(as = ImmutablePricedJob.class)
@Value.Immutable
public interface PricedJob {

    /**
     * @return unique identifier of the given job
     */
    String getJobId();

    /**
     * @return cost of execution of the given job
     */
    double getPriceForJob();

    /**
     * @return power in use for given CNA
     */
    double getPowerInUse();
}
