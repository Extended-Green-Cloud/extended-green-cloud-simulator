package org.greencloud.gui.messages.domain;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableJobCreator.class)
@JsonDeserialize(as = ImmutableJobCreator.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JobCreator extends EventData {

	Long getDeadline();

	Long getDuration();

	Map<String, Resource> getResources();

	String getProcessorName();

	@Nullable
	String getSelectionPreference();

	List<JobStep> getSteps();
}
