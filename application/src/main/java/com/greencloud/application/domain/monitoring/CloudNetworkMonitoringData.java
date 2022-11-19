package com.greencloud.application.domain.monitoring;

import com.database.knowledge.domain.agent.MonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.ClientJob;
import jade.core.AID;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@JsonSerialize(as = CloudNetworkMonitoringData.class)
@JsonDeserialize(as = CloudNetworkMonitoringData.class)
@Value.Immutable
public interface CloudNetworkMonitoringData extends MonitoringData {

    /**
     * @return list of owned Server Agents
     */
    List<AID> getOwnedServers();

    /**
     * @return map of servers and their weights represented as percentages
     */
    Map<AID, Double> getPercentagesForServersMap();

    /**
     * @return map of jobs and their statuses in a given Cloud Network Agents
     */
    Map<ClientJob, JobStatusEnum> getNetworkJobs();
}
