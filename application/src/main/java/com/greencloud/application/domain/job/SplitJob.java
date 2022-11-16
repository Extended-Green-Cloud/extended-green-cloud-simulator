package com.greencloud.application.domain.job;

import java.util.List;

import com.greencloud.commons.job.ClientJob;

public record SplitJob(List<ClientJob> jobParts) {
}
