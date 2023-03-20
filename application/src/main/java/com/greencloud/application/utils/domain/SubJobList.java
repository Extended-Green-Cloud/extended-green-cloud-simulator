package com.greencloud.application.utils.domain;

import java.util.ArrayList;
import java.util.List;

import com.greencloud.commons.domain.job.PowerJob;

/**
 * Class which represents a job list which is used in algorithm finding set of jobs withing given power
 */
public class SubJobList<T extends PowerJob> {

	public final int size;
	public final List<T> subList;

	public SubJobList() {
		this(0, new ArrayList<>());
	}

	public SubJobList(int size, List<T> subList) {
		this.size = size;
		this.subList = subList;
	}
}
