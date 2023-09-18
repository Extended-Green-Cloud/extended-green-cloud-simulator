package org.greencloud.commons.utils.resources.domain;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.domain.job.basic.PowerJob;

/**
 * Class which represents a job list which is used in algorithm finding set of jobs withing given power
 */
public class SubJobList<T extends PowerJob> {

	public final double energySum;
	public final List<T> subList;

	public SubJobList() {
		this(0, new ArrayList<>());
	}

	public SubJobList(double size, List<T> subList) {
		this.energySum = size;
		this.subList = subList;
	}
}
